package cslicer.utils.graph;

import java.io.Serializable;

import cslicer.utils.BytecodeUtils;

public class Identifier implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fId;

	public Identifier(String id) {
		fId = id;
	}

	public String getName() {
		return fId;
	}

	@Override
	public String toString() {
		return fId;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof Identifier))
			return false;
		Identifier id = (Identifier) other;
		return BytecodeUtils.matchWithGenericType(getName(), id.getName());
	}

	@Override
	public int hashCode() {
		return fId.hashCode();
	}
}
