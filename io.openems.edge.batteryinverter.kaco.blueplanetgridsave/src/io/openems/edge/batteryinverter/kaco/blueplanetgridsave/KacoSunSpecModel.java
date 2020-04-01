package io.openems.edge.batteryinverter.kaco.blueplanetgridsave;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OptionsEnum;
import io.openems.edge.bridge.modbus.sunspec.ISunSpecModel;
import io.openems.edge.bridge.modbus.sunspec.SunSpecModelType;
import io.openems.edge.bridge.modbus.sunspec.SunSpecPoint;

public enum KacoSunSpecModel implements ISunSpecModel {
	S_64201(//
			"Bidirectional inverter control", //
			"Bidirectional inverter control backend", //
			"", //
			52, //
			KacoSunSpecModel.S64201.values(), //
			SunSpecModelType.VENDOR_SPECIFIC //
	); //

	public static enum S64201 implements SunSpecPoint {
		VERSION_MAJOR(new PointImpl(//
				"S64201_VERSION_MAJOR", //
				"Version", //
				"Major Version of model", //
				"", //
				PointType.UINT16, //
				true, //
				AccessMode.READ_ONLY, //
				Unit.NONE, //
				null, //
				new OptionsEnum[0])), //
		VERSION_MINOR(new PointImpl(//
				"S64201_VERSION_MINOR", //
				"VerMinor", //
				"Minor Version of model", //
				"", //
				PointType.UINT16, //
				true, //
				AccessMode.READ_ONLY, //
				Unit.NONE, //
				null, //
				new OptionsEnum[0])); //

		protected final PointImpl impl;

		private S64201(PointImpl impl) {
			this.impl = impl;
		}

		@Override
		public PointImpl get() {
			return this.impl;
		}
	}

	public final String label;
	public final String description;
	public final String notes;
	public final int length;
	public final SunSpecPoint[] points;
	public final SunSpecModelType modelType;

	private KacoSunSpecModel(String label, String description, String notes, int length, SunSpecPoint[] points,
			SunSpecModelType modelType) {
		this.label = label;
		this.description = description;
		this.notes = notes;
		this.length = length;
		this.points = points;
		this.modelType = modelType;
	}

	@Override
	public SunSpecPoint[] points() {
		return this.points;
	}

	@Override
	public String label() {
		return this.label;
	}
}
