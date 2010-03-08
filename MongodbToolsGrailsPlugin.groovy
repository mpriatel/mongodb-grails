import org.codehaus.groovy.grails.web.context.GrailsConfigUtils
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import com.iolog.mongodbtools.MongoMapperModel
import com.mongodb.BasicDBObject
import com.iolog.mongodbtools.MongoDbWrapper
import com.iolog.mongodbtools.MongoMapperField

class MongodbToolsGrailsPlugin
{
   def version = "0.1"
   def grailsVersion = "1.2.0 > *"
   def dependsOn = [:]
   def pluginExcludes = [
      "grails-app/views/error.gsp"
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
      application.domainClasses.each { clz ->

         def clazz = clz.clazz
         def typeName = GrailsClassUtils.getStaticPropertyValue(clazz, "mongoTypeName")

         if ( typeName )
         {
            def mongoFields = GrailsClassUtils.getStaticPropertyValue(clazz , "mongoFields")
            def mmm = new MongoMapperModel(clazz,mongoFields)
            mmm.setTypeName(typeName)
            mongo.addMapperModel(typeName,mmm)
         }

         clz.clazz.metaClass.toMongoDoc = {

            def mapper = mongo.getMapperForClass(clazz)
            if ( !mapper )
            {
               def doc = new BasicDBObject()
               doc.putAll( delegate.properties )
            }
            else
            {
               return mapper.buildMongoDoc(delegate)
            }
            
         }
      }


      // go through all the registered mappers, and inspect their fields to see
      // if the field class types are mapped.  if so, associate the mapper
      mongo.mappersByClass.each { mappedClz, mapper ->
         mapper.fields.each { MongoMapperField f ->
            def fieldMapper = mongo.getMapperForClass(f.fieldType)
            if ( fieldMapper ){
               println "associating mapper ${fieldMapper} with ${f.domainFieldName}"
              f.mapper = fieldMapper
            }
         }
      }
   }

   def doWithApplicationContext = { applicationContext ->   }
   def onChange = { event ->   }
   def onConfigChange = { event ->   }
}
