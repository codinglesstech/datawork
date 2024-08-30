package tech.codingless.standalone.datawork.v1.data;

import java.io.Serializable;

public class Tuple3<Item1, Item2, Item3> implements Serializable {
	private static final long serialVersionUID = 1L;
	Item1 item1;
	Item2 item2;
	Item3 item3;
	private boolean success;

	public Tuple3() {
	}

	public Tuple3(Item1 item1, Item2 item2, Item3 item3) {
		this.item1 = item1;
		this.item2 = item2;
		this.item3 = item3;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}

	public Item3 getItem3() {
		return item3;
	}

	public void setItem3(Item3 item3) {
		this.item3 = item3;
	}

	public Item1 getItem1() {
		return item1;
	}

	public Item2 getItem2() {
		return item2;
	}

	public void setItem1(Item1 item1) {
		this.item1 = item1;
	}

	public void setItem2(Item2 item2) {
		this.item2 = item2;
	}
}
