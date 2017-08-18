package de.upb.recalys.helper;

import java.io.Serializable;

/**
 * The Class PairOfInt implements a pair of two Integers.
 *
 * @param <A>
 *            the generic type of the first element.
 * @param <B>
 *            the generic type of the second element.
 */
public class PairOfInt implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1952422388004057960L;
	private Integer first;
	private Integer second;

	/**
	 * Instantiates a new pair.
	 *
	 * @param first
	 *            the first element
	 * @param second
	 *            the second element
	 */
	public PairOfInt(Integer first, Integer second) {
		super();
		this.first = first;
		this.second = second;
	}

	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	public boolean equals(Object other) {
		if (other instanceof PairOfInt) {
			PairOfInt otherPair = (PairOfInt) other;
			return ((this.first == otherPair.first
					|| (this.first != null && otherPair.first != null && this.first.equals(otherPair.first)))
					&& (this.second == otherPair.second || (this.second != null && otherPair.second != null
							&& this.second.equals(otherPair.second))));
		} else {
			return false;
		}
	}

	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	/**
	 * Gets the first element of the pair.
	 *
	 * @return the first element
	 */
	public Integer getFirst() {
		return first;
	}

	/**
	 * Gets the second element of the pair.
	 *
	 * @return the second element
	 */
	public Integer getSecond() {
		return second;
	}

	/**
	 * Sets the pair.
	 *
	 * @param first
	 *            the first element
	 * @param second
	 *            the second element
	 */
	public void setPair(Integer first, Integer second) {
		this.first = first;
		this.second = second;
	}

}
