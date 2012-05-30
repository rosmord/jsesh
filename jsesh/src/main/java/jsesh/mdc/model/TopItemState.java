/*
 * Created on 5 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdc.model;

import java.io.Serializable;

/**
 * Describe the state of a top item in terms of manuel de codage toggles :
 * is it part of a grayed area ? is it red ?
 * 
 * 
 * @author rosmord
 *
 */
public class TopItemState implements Cloneable, Comparable<TopItemState>, Serializable
{
	private static final long serialVersionUID = 9076888292349625796L;
	private boolean isRed= false;
	private boolean isShaded= false;

	public TopItemState duplicate()
	{
		TopItemState result= null;
		try
		{
			result= (TopItemState) clone();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * @return true if red.
	 */
	public boolean isRed()
	{
		return isRed;
	}

	/**
	 * @return true if shaded.
	 */
	public boolean isShaded()
	{
		return isShaded;
	}

	/**
	 * @param b
	 */
	public void setRed(boolean b)
	{
		isRed= b;
	}

	/**
	 * @param b
	 */
	public void setShaded(boolean b)
	{
		isShaded= b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		boolean result= false;
		if (obj instanceof TopItemState)
		{
			TopItemState st= (TopItemState) obj;
			result= (isRed= st.isRed) && (isShaded == st.isShaded);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result= 0;
		if (isShaded)
			result++;
		if (isRed)
			result++;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException
	{
		TopItemState result= (TopItemState) super.clone();
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TopItemState s) {
		int result= (isRed?1:0) - (s.isRed?1:0);
		if (result==0)
			result= (isShaded?1:0) - (s.isShaded?1:0);
		return result;
	}
	
	

}
