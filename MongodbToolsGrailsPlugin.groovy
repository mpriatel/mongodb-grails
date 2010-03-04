import org.codehaus.groovy.grails.web.context.GrailsConfigUtils
import org.codehaus.groovy.grails.commons.GrailsClassUtils

class MongodbToolsGrailsPlugin
{
   // the plugin version
   def version = "0.1"
   // the version or versions of Grails the plugin is designed for
   def grailsVersion = "1.2.1 > *"
   // the other plugins this plugin depends on
   def dependsOn = [:]
   // resources that are excluded from plugin packaging
   def pluginExcludes = [
      "grails-app/views/error.gsp"
   ]

   // TODO Fill in these fields
   def author = "Your name"
   def authorEmail = ""
   def title = "Plugin summary/headline"
   def description = '''\\
Brief description of the plugin.
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
      application.domainClasses.each { clz ->
         def typeName = GrailsClassUtils.getStaticPropertyValue(clz.clazz, "mongoTypeName")

         if ( typeName )
         {
            def mongoFields = GrailsClassUtils.getStaticPropertyValue(clz.clazz , "mongoFields")   
         }



      }
   }

   def doWithApplicationContext = { applicationContext ->   }
   def onChange = { event ->   }
   def onConfigChange = { event ->   }
}
