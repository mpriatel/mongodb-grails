package com.iolog.mongodbtools

import org.springframework.web.servlet.ModelAndView
import com.mongodb.BasicDBObject

class MongoStatusController
{

	def mongo


	def index = {
		redirect( action:'servers' )
	}




	def servers = {                             

//		mongo.addMappedClass( LogData.class )
//
//		LogData d = new LogData( eventType:99 , count:20 )
//		d.mongoInsert( mongo.server1.logdata.ddd )


		def serverData = [];

		mongo.serverConfigs.each { id , config ->
			def mongoDb = mongo."${id}";
			def status = mongoDb.admin.command( [serverStatus:1] as BasicDBObject );
			def databases = mongoDb.admin.command( [listDatabases:1] as BasicDBObject )
			serverData << [ id:id , config:config , status:status , dbs: databases ]
		}

		def model = [:]
		model.servers = serverData

		return new ModelAndView("/mongoStatus/main", model)
	}



	def collection = {



		def col = mongo."${params.server}"."${params.db}"."${params.colName}"
//
		def rs = col.find().each {
			render "<div>${it}</div>"
		};




	}


}


class LogData{

	String _id
	int eventType
	int count

	static mongoTypeName = 'ld'
	static mongoFields = [ et : 'eventType' , c : 'count' ]


}
