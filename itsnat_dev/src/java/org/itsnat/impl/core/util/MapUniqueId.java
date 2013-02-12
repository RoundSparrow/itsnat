/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2011 Jose Maria Arranz Santamaria, Spanish citizen

  This software is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 3 of
  the License, or (at your option) any later version.
  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details. You should have received
  a copy of the GNU Lesser General Public License along with this program.
  If not, see <http://www.gnu.org/licenses/>.
*/

package org.itsnat.impl.core.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.itsnat.core.ItsNatException;

/**
 *
 * @author jmarranz
 */
public class MapUniqueId implements Serializable
{
    protected Map<String,HasUniqueId> map = new HashMap<String,HasUniqueId>();
    protected UniqueIdGenerator generator;

    /** Creates a new instance of MapUniqueId */
    public MapUniqueId(UniqueIdGenerator generator)
    {
        this.generator = generator;
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    public int size()
    {
        return map.size();
    }

    public void clear()
    {
        map.clear();
    }

    public boolean containsKey(HasUniqueId obj)
    {
        check(obj);
        UniqueId idObj = obj.getUniqueId();
        String id = idObj.getId();
        return map.containsKey(id);
    }

    public void putAll(MapUniqueId otherMap)
    {
        check(otherMap.generator);
        map.putAll(otherMap.map);
    }

    public Set<Map.Entry<String,HasUniqueId>> entrySet()
    {
        return map.entrySet();
    }

    public Collection<HasUniqueId> values()
    {
        return map.values();
    }

    public HasUniqueId get(String id)
    {
        return map.get(id);
    }

    public HasUniqueId put(HasUniqueId obj)
    {
        check(obj);
        UniqueId idObj = obj.getUniqueId();
        String id = idObj.getId();
        return map.put(id,obj);
    }

    public HasUniqueId removeById(String id)
    {
        return map.remove(id);
    }

    public HasUniqueId remove(HasUniqueId obj)
    {
        check(obj);
        UniqueId idObj = obj.getUniqueId();
        String id = idObj.getId();
        return map.remove(id);
    }

    public HasUniqueId[] toArray(HasUniqueId[] array)
    {
        if (array.length != size()) throw new ItsNatException("INTERNAL ERROR");
        int i = 0;
        for(Iterator<Map.Entry<String,HasUniqueId>> it = map.entrySet().iterator(); it.hasNext(); i++)
        {
            Map.Entry<String,HasUniqueId> entry = it.next();
            HasUniqueId value = entry.getValue();
            array[i] = value;
        }
        return array;
    }

    public void check(HasUniqueId idObj)
    {
        check(generator,idObj);
    }

    public void check(UniqueIdGenerator generator)
    {
        // La unicidad s�lo est� asegurada dentro de los ids generados
        // por el generador dado.
        // Evitamos que una colecci�n tipo Map o Set metamos como clave
        // ids generados por diferentes generadores.
        if (this.generator != generator)
            throw new ItsNatException("INTERNAL ERROR");
    }

    public static void check(UniqueIdGenerator generator,HasUniqueId obj)
    {
        if (generator != obj.getUniqueId().getUniqueIdGenerator())
            throw new ItsNatException("INTERNAL ERROR");
    }
}
