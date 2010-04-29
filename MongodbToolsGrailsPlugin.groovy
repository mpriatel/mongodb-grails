import org.codehaus.groovy.grails.web.context.GrailsConfigUtils
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import com.iolog.mongodbtools.MongoMapperModel
import com.mongodb.BasicDBObject
import com.iolog.mongodbtools.MongoDbWrapper
import com.iolog.mongodbtools.MongoMapperField
import com.mongodb.ObjectId

class MongodbToolsGrailsPlugin
{
	def version = "0.1"
	def grailsVersion = "1.2.0 > *"
	def dependsOn = [:]
	def pluginExcludes = [
		 "grails-app/views/error.gsp",
		 "grails-app/config/Config.groovy",  // just used for stand-alone testing
		 "grails-app/domain/com/acme/AnnotatedWidget.groovy",
		 "grails-app/domain/com/acme/MappedWidget.groovy"	
	]

	def author = "Mark Priatel"
	def authorEmail = "mpriatel@gmail.com"
	def title = "MongoDB Tools"
	def description = '''\\
      This plugin makes working with MongoDB a little bit more groovy by adding several features such as:
      a domain object <-> MongoDB Document mapper, database connection management, a groovy-builder to
      create MongoDB documents.
      '''

	// URL to the plugin's documentation
	def documentation = "http://grails.org/plugin/mongodb-tools"



	def doWithWebDescriptor = { xml ->
		// TODO Implement additions to web.xml (optional), this event occurs before
	}

	def doWithSpring = {
		mongo(com.iolog.mongodbtools.MongoDbWrapper) {
			grailsApplication = application
		}
	}


	def doWithDynamicMethods = { ctx ->

		MongoDbWrapper mongo = ctx.getBean('mongo')

      // send all domain classes to be mapped
      // ------------------------------------
		application.domainClasses.each { clz ->
			def clazz = clz.clazz
			mongo.addMappedClass(clazz)
		}

		// go through all the registered mappers, and inspect their fields to see
		// if the field class types are mapped.  if so, associate the mapper
      // ----------------------------------------------------------------------
		mongo.mappersByClass.each { mappedClz, mapper ->
			mapper.fields.each { MongoMapperField f ->
				def fieldMapper = mongo.getMapperForClass(f.fieldType)
				if (fieldMapper)
				{
					f.mapper = fieldMapper
				}
			}
		}
	}

	def doWithApplicationContext = { applicationContext ->   }
	def onChange = { event ->   }
	def onConfigChange = { event ->   }
}
