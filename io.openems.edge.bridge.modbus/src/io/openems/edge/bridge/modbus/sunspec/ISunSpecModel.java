package io.openems.edge.bridge.modbus.sunspec;

public interface ISunSpecModel {

	public String name();
	
	public String label();
	
	public SunSpecPoint[] points();
	
}
