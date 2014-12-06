package com.cs492.drone_model.attributes.parameters;

import com.MAVLink.common.msg_param_value;

public class Parameter implements Comparable<Parameter> {

	public String name;
	public double value;
	public int type;

	protected Parameter(String name, double value, int type) {
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public Parameter(msg_param_value m_value) {
		this(m_value.getParam_Id(), m_value.param_value, m_value.param_type);
	}

	public Parameter(Parameter parameter) {
		this(parameter.name, parameter.value, parameter.type);
	}

	public double getValue() { 
		return value;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parameter)) return false;

        Parameter parameter = (Parameter) o;

        return (name == null ? parameter.name == null : name.equals(parameter.name));
    }

    @Override
    public int compareTo(Parameter another) {
        return name.compareTo(another.name);
    }
}
