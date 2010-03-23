package com.iolog.mongodbtools;

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

import com.mongodb.Mongo;
import org.springframework.beans.factory.InitializingBean
import com.mongodb.DBApiLayer
import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import com.mongodb.DBCollection
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import com.mongodb.ObjectId
import com.mongodb.gridfs.GridFS
import com.mongodb.DB
import org.springframework.web.multipart.commons.CommonsMultipartFile
import com.mongodb.gridfs.GridFSInputFile
import com.mongodb.DBObject

/**
 * <p>The MongoDbWrapper is exposed to Grails applications as a Spring bean called 'mongo'.
 * Grails classes can then easily make use of MongoDbWrapper by simply adding a 'mongo'
 * property to their class, eg:
 *
 * <pre>
 * class UserController{*    def mongo
 *}* </pre>
 *
 * <h3>Configuration</h3>
 *
 * <p>At startup the MongoDbWrapper will look for a 'mongo' configuration property in your
 * Config.groovy file which defines your MongoDB databases and related configuration
 * properties.
 *
 * <pre>
 * #Config.groovy
 *
 * mongo{*    databases{*       server1{*          host = "localhost"
 *          port = 1234  // if omited, will use the default MongoDB port
 *}*
 *       server2{*          host = "192.168.1.2"
 *}*}*
 *
 *    shortcuts{*       users = "server1/app/users"
 *       comments = "server2/app/comments"
 *}*}* </pre>
 *
 * <p>The above example registers two different database hosts which can then be accessed using the mongo
 * bean: <code>mongo.server1.&lt;dbname&gt;.&lt;collection&gt;</code>.  'dbname' and 'collection' will
 * return the corrisponding Java MongoDB driver equivalents (DB, DBCollection). 
 *
 * <h3>Shortcuts</h3>
 *
 * <p>Shortcuts can also be defined to shorten the syntax required to access a collection by registering a
 * root-level mongo property directly with a collection.  For example, in the above
 * example we mapped the 'users' shortcut to "server1/app/users", which lets us use <code>mongo.users</code>
 * instead of <code>mongo.server1.app.users</code>.
 *
 * <p>Shortcuts have the added benefit of making it easier to change your server topology without having to
 * change your code;  if you were to move your users collection to a different server, you would just update
 * your alias.
 *
 * <p>Shortcuts will also be used in the future to reference server-pools.
 *
 * <h3>Mapping Domain Objects</h3>
 *
 * <p>The MongoDbWrapper makes it easy to save and restore your Grails domain objects
 * to a collection by using mappers which convert your domain objects to BasicDBObjects,
 * and vice-versa.
 *
 * <p>To register your Domain class with a mapper you need to add two static helper fields
 * to your class:
 *
 * <pre>
 * class User{*   String firstName
 *   String lastName
 *
 *   static mongoTypeName = "user"
 *   static mongoMappedFields = ['fn':firstName','ln':'lastName']
 *}* </pre>
 *
 * <p>When your object are converted to documents a property '_t' is added to help identify the type.
 * This type identifier is specified with <code>mongoTypeName</code>.  You then specify which fields
 * should be saved, and their respective keys.
 *
 * <p>Domain objects can then be coverted to docs by calling the MOP added method "<code>toMongoDoc()</code>".
 *
 * <pre>
 *   def user = new User( firstName:"mark", lastName:"priatel" )
 *   def userDoc = user.toMongoDoc()
 * </pre>
 *
 * <p>The mapper will also process mapped properties and Lists:
 *
 * <pre>
 * class Address{*    String city
 *    String country
 *
 *    static mongoTypeName = "address"
 *    static mongoMappedFields = [ 'ci':'city' , 'co' : 'country' ]
 *}*
 * class User{*   String firstName
 *   String lastName
 *   Address address
 *
 *   static mongoTypeName = "user"
 *   static mongoMappedFields = ['fn':firstName','ln':'lastName','adrs':'address']
 *}*
 * def adrs = new Address( city:'ottawa' , country:'canada' )
 * def user = new User( firstName:'mark', lastName:'priatel',address:adrs)
 *
 * mongo.users.save( user.toMongoDoc() )
 *
 * (bson) { "_id" : ObjectId("4b952284d8e992502c9629e3"), "_t" : "u", "fn" : "mark", "ln" : "priatel", "adrs" : { "_t" : "a", "ci" : "ottawa", "co" : "canada" }}*
 * </pre>
 *
 *
 *
 *
 *
 * <h3>MongoDB Document Builder</h3>
 *
 * <p>The mongo bean exposes a special root-level property 'doc' which can be used to create
 * BasicDBObjectS (which are used by the Java driver to represent MongoDB documents) using a
 * Groovy builder syntax:
 *
 * <pre>
 *
 * def userData = mongo.doc{*    firstName("mark")
 *    lastName("priatel")
 *    company("iolog")
 *    address{*       city("ottawa")
 *       country("canada")
 *}*}*
 * mongo.user.save(userData)
 *
 * println userData._id
 * </pre>
 *
 *
 * @author Mark Priatel <mark@iolog.com>
 */
public class MongoDbWrapper implements InitializingBean
{
	def grailsApplication

	Map<String, Mongo> mongos = new HashMap<String, Mongo>();
	Map<String, Object> shortcuts = new HashMap<String, Object>();
	Map<String, Class> typeMappings = new HashMap<String, Class>()
	Map<String, MongoMapperModel> mappersByTypeName = new HashMap<String, MongoMapperModel>()
	Map<Class, MongoMapperModel> mappersByClass = new HashMap<Class, MongoMapperModel>()
	Map<Class, String> typeForClassMap = new HashMap<Class, String>()
	Map<String, Map> serverConfigs = new HashMap<String, Map>()
   Map<String,GridFS> fileGrids = new HashMap<String,GridFS>()

	/**
	 *
	 * @return
	 */
	public Map<String, MongoMapperModel> getMappers() { return mappersByTypeName }

	/**
	 *
	 */
	public MongoMapperModel getMapper(String typeName) { return mappersByTypeName.get(typeName) }

	/**
	 *
	 */
	public MongoMapperModel getMapperForClass(Class c) { return mappersByClass.get(c) }

	/**
	 * given a Class, this method returns the typeName used to associate a Mongo record/document
	 * with a class
	 *
	 */
	public String getTypeNameForClass(Class) { return typeForClassMap.get(c) }

	/**
	 * add support so that in Groovy, databases can be referenced using
	 * mongo.<database>
	 * @param key
	 * @return
	 */
	def Object propertyMissing(String name)
	{
		if (mongos.containsKey(name)) return mongos.get(name);
		if (shortcuts.containsKey(name)) return shortcuts.get(name);
      if (fileGrids.containsKey(name)) return fileGrids.get(name);

		// if no MongoDB Is registered for the given key, lookup its
		// connection details in grailsApplication.config
		def dbConfig = grailsApplication.config.mongo.databases?."${name}"
		if (!dbConfig)
		{
			throw new IllegalArgumentException("No MongoDB config found in grailsApplicaiton.config.mongo.databases.${name}")
		}

		def port = dbConfig.port ? dbConfig.port : 27017
		Mongo mongo = new Mongo(dbConfig.host, port)
		mongos.put(name, mongo)
		return mongo
	}

	/**
	 * Makes instances of clazz mappable, so that they can be saved directly into MongoDB.
	 */
	def void addMappedClass(Class clazz)
	{
		if ( mappersByClass.containsKey(clazz) ) return;

		def typeName = GrailsClassUtils.getStaticPropertyValue(clazz, "mongoTypeName")

		if (typeName)
		{
			def mongoFields = GrailsClassUtils.getStaticPropertyValue(clazz, "mongoFields")
			def mmm = new MongoMapperModel(clazz, mongoFields)
			mmm.setTypeName(typeName)
			this.addMapperModel(typeName, mmm)
		}

		/**
		 * inserts the Domain object into mongo and assigns the generated mongo document id
		 * to _id
		 */
		clazz.metaClass.mongoInsert = { coll ->
			def doc = delegate.toMongoDoc()
			coll.insert(doc)
			delegate._id = doc._id
		}


		clazz.metaClass.mongoRemove = { coll ->
			if (delegate._id)
			{
				coll.remove(new BasicDBObject([_id: delegate._id]))
			}
		}

		clazz.metaClass.mongoUpdate = { coll, obj ->
			coll.update(
				 [_id: new ObjectId(delegate._id)] as BasicDBObject,
				 obj as BasicDBObject,
				 false,
				 false
			)
		}

		clazz.metaClass.mongoRemove = { coll ->
			coll.remove([_id: new ObjectId(delegate._id)] as BasicDBObject)
		}

      clazz.metaClass.getByMongoId = { id, coll = false ->
         coll.find( [_id: new ObjectId(delegate._id) ] as BasicDBObject )
      }


		clazz.metaClass.toMongoDoc = {
			def mapper = this.getMapperForClass(clazz)
			if (!mapper)
			{
				def doc = new BasicDBObject()
				doc.putAll(delegate.properties)
			}
			else
			{
				return mapper.buildMongoDoc(delegate)
			}
		}
	}

	/**
	 * during the plugin initialization domain classes which have a static
	 * 'mongoFields' property will have a MongoMapperModel created for them
	 * which are then added to the wrapper.
	 */
	public void addMapperModel(String typeName, MongoMapperModel mmm)
	{

		typeForClassMap.put(mmm.clazz, typeName)
		mappersByTypeName.put(typeName, mmm)
		mappersByClass.put(mmm.clazz, mmm)
	}

	/**
	 * add Groovy helpers to MongoDB objects.
	 */
	void afterPropertiesSet()
	{

		grailsApplication.config.mongo.databases.each { dbId, config ->
			serverConfigs.put(dbId, config)
		}



		Mongo.metaClass.propertyMissing = { String name ->
			return ((Mongo) delegate).getDB(name)
		}

      GridFS.metaClass.createFile = { CommonsMultipartFile file ->
         GridFSInputFile gridFile = delegate.createFile(file.getInputStream(), file.getOriginalFilename() )
         gridFile.setContentType( file.getFileItem().getContentType() )
         gridFile.setFilename( file.getOriginalFilename() )

         DBObject metaData = gridFile.getMetaData()
         metaData.put("fileSize", file.getSize() )

         return gridFile
      }

		DBApiLayer.metaClass.propertyMissing = { String name ->
			return ((DBApiLayer) delegate).getCollection(name)
		}

		DBCollection.metaClass.update = { Map crit, Map obj, Map options ->
			boolean multi = false
			boolean upsert = false
			if (options)
			{
				multi = options?.multi ?: false
				upsert = options?.upsert ?: false
			}

			delegate.update(crit as BasicDBObject, obj as BasicDBObject, upsert, multi)
		}

      DBCollection.metaClass.findById = { String id ->
         BasicDBObject query = new BasicDBObject();
         query.put('_id' , new ObjectId(id) )
         return delegate.findOne( query ) 
      }

		BasicDBList.metaClass.toObject = {
			List oList = new ArrayList((int) delegate.size())
			delegate.each {
				if (it instanceof BasicDBObject || it instanceof BasicDBList) oList.add(it.toObject())
				else oList.add(it)
			}
		}

		Collection.metaClass.toMongoDoc = {
			BasicDBList newList = new BasicDBList()
			delegate.each {
				def mapper = mappersByClass.get(it.getClass())
				if (mapper) newList << mapper.buildMongoDoc(it)
				else newList << it
			}
			return newList
		}

		BasicDBObject.metaClass.toObject = {
			def typeName = delegate.get("_t")

			if (!typeName)
			{                                				return delegate
			}

			BasicDBObject _self = (BasicDBObject) delegate
			MongoMapperModel mmm = mappersByTypeName.get(typeName)

			if (!mmm)
			{
				return delegate
			}

			// begin mapping process to create a new object using
			// information stored inthe MongoMapperModel

			def obj = mmm.clazz.newInstance()
			obj._id = _self._id

			for (MongoMapperField mmf: mmm.fields)
			{
				def fieldVal = _self.get(mmf.mongoFieldName)

				// if this field is mapped, unwrap it and assign it to the domain field
				// --------------------------------------------------------------------
				if (mmf.mapper)
				{
					obj."${mmf.domainFieldName}" = fieldVal.toObject()
					continue
				}

				switch (mmf.fieldType)
				{
					case String:
						obj."${mmf.domainFieldName}" = _self.get(mmf.mongoFieldName)
						break


					case List:
						BasicDBList mongoList = (BasicDBList) _self.get(mmf.mongoFieldName)
						if (!mongoList)
						{
							obj."${mmf.domainFieldName}" = new ArrayList()
						}
						else
						{
							obj."${mmf.domainFieldName}" = mongoList.toObject()
						}

						break

				}


			}

			return obj
		}

		grailsApplication.config.mongo.shortcuts.each { shortcutName, String path ->
			def pathParts = path.split("\\/")
			def obj = this."${pathParts[0]}"."${pathParts[1]}"."${pathParts[2]}"
			if (obj) shortcuts.put(shortcutName, obj)
		}



      grailsApplication.config.mongo.gridFS.each { id , config ->
         DB db = this."${config.server}"."${config.database}"
         String bucket = config.bucket
         GridFS gridFS
         if ( bucket ) gridFS = new GridFS(db,bucket)
         else gridFS = new GridFS(db)

         
         fileGrids.put( id, gridFS )
      }

	}

	/**
	 * Returns a MongoDocBuilder that makes it possible to create BasicDBObjects using
	 * groovy builder syntax. Exposing a getDoc() method makes it possible to create
	 * builders on-the-fly, for example:
	 *
	 * mongo.doc{*    name('jim')
	 *    age(10)
	 *}}*/
	public BasicDBObject doc(Closure c)
	{
		MongoDocBuilder db = new MongoDocBuilder(this)
		c.setDelegate(db)
		c.call()
		return c.root

	}


}
