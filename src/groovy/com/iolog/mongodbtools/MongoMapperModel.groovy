package com.iolog.mongodbtools

/**
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

import com.mongodb.BasicDBObject
import org.codehaus.groovy.grails.commons.GrailsClassUtils

/**
 * @author Mark Priatel <mark@iolog.com>
 */
class MongoMapperModel {

    Class clazz
    MongoMapperField[] fields
    String typeName

    public MongoMapperModel(Class clazz, Map fieldNames) {
        this.clazz = clazz;
        setMappedFields(fieldNames)
    }

    /**
     * proccesses a map of mongoFieldName -> domainFieldName to populate the MongoMapperModel's
     * field definitions.  These are then used during the restore/save process
     * @param fieldNames
     */
    public void setMappedFields(Map fieldNames) {
        fields = new MongoMapperField[fieldNames.size()]
        def idx = 0
        fieldNames.each { alias, fieldName ->
            MongoMapperField mmf = new MongoMapperField();

            Map hasManyDef = GrailsClassUtils.getStaticPropertyValue(this.clazz, "hasMany")
            if (hasManyDef && hasManyDef.containsKey(fieldName)) {
                mmf.isGrailsHasMany = true
                mmf.fieldType = hasManyDef.get(fieldName)
            }
            else {
                Class type = GrailsClassUtils.getPropertyType(this.clazz, fieldName)
                mmf.fieldType = type
            }

            mmf.domainFieldName = fieldName
            mmf.mongoFieldName = alias

            fields[idx++] = mmf
        }
    }

    /**
     * creates a BasicDBObject suitable for saving to Mongo which contains
     * the mapped field for this domain object.  If the provided object is
     * not of the same class that this mapper supports, an exeption will be thrown
     */
    public BasicDBObject buildMongoDoc(Object o) {
        if (o == null) return null
        if (o.getClass() != clazz) throw new IllegalArgumentException("mapper (${clazz}) cannot process object for class ${o.class}");
        BasicDBObject doc = new BasicDBObject()
        doc.put("_t", typeName)
        fields.each { f ->
            def val = o."${f.domainFieldName}"
            if (val) {
                if (f.mapper) {
                    if (f.isGrailsHasMany) {
                        doc.put(f.mongoFieldName, val.toMongoDoc())
                    }
                    else {
                        doc.put(f.mongoFieldName, f.mapper.buildMongoDoc(val))
                    }
                }
                else if (val instanceof Collection) {
                    doc.put(f.mongoFieldName, val.toMongoDoc())
                }
                else // no mapper associated, store the object as-is
                {
                    doc.put(f.mongoFieldName, val)
                }
            }
        }
        return doc
    }

}



