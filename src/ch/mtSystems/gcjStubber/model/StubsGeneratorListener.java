/*
 *   GcjStubber - A stub creator for GCJ (JNC).
 *   Copyright (C) 2007  Marco Trudel <mtrudel@gmx.ch>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.mtSystems.gcjStubber.model;

public interface StubsGeneratorListener
{
	public void started();
	public void actionDone(String msg);

	/**
	 * Notifies about an complete phase for an object.
	 *  
	 * @param objectName The name of the processed object.
	 * @param phaseProcessed The phase:
	 *                       - 0: Skipped (No Java Classes)
	 *                       - 1: Skipped (Won't be pulled in)
	 *                       - 2: Phase 1
	 *                       - 3: Phase 2
	 *                       - 4: Phase 3
	 * @param phaseResult The result of the phase:
	 *                    - 0: Ok
	 *                    - 1: Failed
	 *                    - 2: Problem
	 * @param phaseResultMsg If the phase result is 1 or 2, this contains a problem description.
	 * @param objectIndex The index of the object (1 ... totalCount)
	 * @param totalCount The total count of all objects.
	 */
	public void processed(String objectName, int phaseProcessed, int phaseResult,
			String phaseResultMsg, int objectIndex, int totalCount);
	
	public void done();
}
