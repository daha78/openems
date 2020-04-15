package io.openems.edge.battery.soltaro.single.versionc.statemachine;

import io.openems.edge.common.statemachine.StateHandler;

public class Undefined extends StateHandler<State, Context> {

	@Override
	public State getNextState(Context context) {
		if (context.config.switchedOn()) {
			// Switched ON
			if (context.component.hasFaults()) {
				// Has Faults -> error handling
				return State.ERROR_HANDLING;
			} else {
				// No Faults -> start
				return State.GO_RUNNING;
			}
		} else {
			// Switched OFF
			return State.GO_STOPPED;
		}
	}

}
