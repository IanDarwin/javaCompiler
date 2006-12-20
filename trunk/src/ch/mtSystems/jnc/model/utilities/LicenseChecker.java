/*
 *   JavaNativeCompiler - A Java to native compiler.
 *   Copyright (C) 2006  Marco Trudel <mtrudel@gmx.ch>
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

package ch.mtSystems.jnc.model.utilities;

import java.lang.reflect.Method;

public class LicenseChecker
{
	public static boolean isLicenseValid(String license)
	{
		try
		{
			Class c = Class.forName("ch.mtSystems.jnc.licence.LicenseUtilities");
			Method m = c.getMethod("isLicenseValid", String.class);
			return (Boolean)m.invoke(null, new Object[] { license });
		} catch(Exception ex)
		{
			return true;
		}
	}
}
