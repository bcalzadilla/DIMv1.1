package com.thingworx.sdk.android.steamexample;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinitions;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.metadata.collections.FieldDefinitionCollection;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.constants.CommonPropertyNames;
import com.thingworx.types.primitives.NumberPrimitive;
import com.thingworx.types.primitives.StringPrimitive;

import org.joda.time.DateTime;


@SuppressWarnings("serial")
@ThingworxPropertyDefinitions(properties = {
		@ThingworxPropertyDefinition(name="Temperature", description="Current Temperature", baseType="NUMBER", category="Status", aspects={"isReadOnly:true"}),
		@ThingworxPropertyDefinition(name="Pressure", description="Current Pressure", baseType="NUMBER", category="Status", aspects={"isReadOnly:true"}),
		@ThingworxPropertyDefinition(name="FaultStatus", description="Fault status", baseType="BOOLEAN", category="Faults", aspects={"isReadOnly:true"}),
		@ThingworxPropertyDefinition(name="InletValve", description="Inlet valve state", baseType="BOOLEAN", category="Status", aspects={"isReadOnly:true"}),
		@ThingworxPropertyDefinition(name="TemperatureLimit", description="Temperature fault limit", baseType="NUMBER", category="Faults", aspects={"isReadOnly:false"}),
		@ThingworxPropertyDefinition(name="TotalFlow", description="Total flow", baseType="NUMBER", category="Aggregates", aspects={"isReadOnly:true"}),
		@ThingworxPropertyDefinition(name="proplatitud", description="latitud", baseType="NUMBER", category="Status", aspects={"isReadOnly:false"}),
	})

	@ThingworxEventDefinitions(events = {
		@ThingworxEventDefinition(name="SteamSensorFault", description="Steam sensor fault", dataShape="SteamSensor.Fault", category="Faults", isInvocable=true, isPropertyEvent=false)
	})

public class SteamThing extends VirtualThing {

	double totalFlow = 0.0;
	int scanCount = 0;

	public SteamThing(String name, String description, ConnectedThingClient client) throws Exception {
		super(name,description,client);
		
		// Define the Data Shape used by the SteamSensor.Fault event.
		FieldDefinitionCollection faultFields = new FieldDefinitionCollection();
		faultFields.addFieldDefinition(new FieldDefinition(CommonPropertyNames.PROP_MESSAGE, BaseTypes.STRING));
		defineDataShapeDefinition("SteamSensor.Fault", faultFields);

		// Copy all the default values from the aspects of the properties defined above to this
		// instance. This gives it an initial state but does not push it to the server.
		initializeFromAnnotations();
	}

	/**
	 * The application that binds this Thing to its connection is responsible for calling this method
	 * periodically to allow this Thing to generate simulated data. If your application generates
	 * data instead of simulating it, your would update your properties when new data is available
	 * and then call updateSubscribedProperties() to push these values to the server. This method
	 * can also be used to poll your hardware if it does not deliver its own data asynchronously.
	 * @throws Exception
	 */
	@Override
	public void processScanRequest() throws Exception {
		double temperature = 400 + 40 * Math.random();
		double pressure = 18 + 5 * Math.random();
		double latitud = 18 + 5 * Math.random();
		totalFlow += Math.random();

		boolean inletValveStatus = true;

		scanCount++;
		if(scanCount>3){
			scanCount=0;
			inletValveStatus = !inletValveStatus;
		}

		setProperty("Temperature", temperature);
		MainActivity loc = new MainActivity();
		setProperty("proplatitud", loc.latitud);
		setProperty("Pressure", pressure);
		setProperty("TotalFlow", totalFlow);
		setProperty("InletValve", inletValveStatus);

		double temperatureLimit = (Double)getProperty("TemperatureLimit").getValue().getValue();

		boolean faultStatus = false;

		if(temperatureLimit > 0 && temperature > temperatureLimit)
			faultStatus = true;

		if(faultStatus) {
			boolean previousFaultStatus = (Boolean)getProperty("FaultStatus").getValue().getValue();

			if(!previousFaultStatus) {
				ValueCollection eventInfo = new ValueCollection();

				eventInfo.put(CommonPropertyNames.PROP_MESSAGE, new StringPrimitive("Temperature at " + temperature + " was above limit of " + temperatureLimit));

				queueEvent("SteamSensorFault", DateTime.now(), eventInfo);
			}
		}

		setProperty("FaultStatus", faultStatus);

		updateSubscribedProperties(15000);
		updateSubscribedEvents(60000);

	}

	/**
	 * A utility method which immediately pushes the TemperatureLimit property to the server.
	 * @param limit the temperature value which will cause a fault event to be generated.
	 * @throws Exception
	 */
	protected void setTemperatureLimit(Double limit) throws Exception {
		setProperty("TemperatureLimit", new NumberPrimitive(limit));
		updateSubscribedProperties(15000);
	}

	/**
	 * This sample method will be available to be bound and can be called from the server.
	 * It adds its two parameters together and returns the result.
	 * @param a the first addend
	 * @param b the second addend
	 * @return the sum of a + b
	 * @throws Exception
	 */
	@ThingworxServiceDefinition( name="AddNumbers", description="Add Two Numbers")
	@ThingworxServiceResult( name="result", description="Result", baseType="NUMBER" )
	public Double AddNumbers( 
			@ThingworxServiceParameter( name="a", description="Value 1", baseType="NUMBER" ) Double a,
			@ThingworxServiceParameter( name="b", description="Value 2", baseType="NUMBER" ) Double b) throws Exception {
		
		return a + b;
	}

	/**
	 * This sample method will be available to be bound and can be called from the server.
	 * It returns a very large (24K) sample string.
	 * @return a large string.
	 */
	@ThingworxServiceDefinition( name="GetBigString", description="Get big string")
	@ThingworxServiceResult( name="result", description="Result", baseType="STRING" )
	public String GetBigString() {
		StringBuilder sbValue = new StringBuilder();
		
		for(int i=0;i<24000;i++) {
			sbValue.append('0');
		}
		
		return sbValue.toString();
	}

	/**
	 * This sample method will be available to be bound and can be called from the server.
	 * When this method is called from the server it will cause this steam client to disconnect.
	 * @throws Exception
	 */
	@ThingworxServiceDefinition( name="Shutdown", description="Shutdown the client")
	public void Shutdown() throws Exception {
		this.getClient().shutdown();
	}

}
