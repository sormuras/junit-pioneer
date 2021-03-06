/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

class FloatRange extends Range<Float> {

	public FloatRange(FloatRangeSource source) {
		super(source.from(), source.to(), source.step(), source.closed());
	}

	@Override
	public Float nextValue() {
		return getCurrent() + getStep();
	}

	@Override
	Float getZero() {
		return 0.0F;
	}

}
