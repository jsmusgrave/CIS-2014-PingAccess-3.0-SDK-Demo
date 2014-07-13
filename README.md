CIS-2014-PingAccess-3.0-SDK-Demo
================================

PingAccess 3.0 SDK Sample for cloudidentitysummit.com 2014.

Overview
--------
This is a sample for a live code demo at [cloudidentitysummit.com 2014][CIS2014].  It uses the PingAccess SDK and 
therefore requires access to the internet.  If you don't have internet access, but have access to the PingAccess install you can [use this alternative.](has-no-tubes)

Objective
---------
Create a Rule using the PingAccess SDK that only allows access to requests with matching [User-Agents][userAgent] defined.  The rule UI will contain a single text field that can be used as a Regex to validate the User-Agent field.

Prerequisites
-------------
To build this sample, you need to have the following installed:

* [Maven 3.x](apache-maven)
* [Git 1.8.x](git)
* [Java 7](java)

Begin
-----

1. Clone this repository (if you haven't already.)  We start with a maven pom referencing the PingAccess SDK.  The repo starts out complete

1. By default this repository's master is complete and working.  That's no way to begin a sample if you're going to work though it!  Let's revert by checking out the BEGIN branch.

		$git checkout BEGIN

1. Examine the Rule class.  Now we see that the Rule has been mostly blanked out.  It's just a simple Rule that extends *com.pingidentity.pa.sdk.policy.RuleInterceptorBase*.  *RuleInterceptorBase* is a simple base class to simplify, you don't have to extend it and could merely implement the interface *com.pingidentity.pa.sdk.policy.RuleInterceptor*.  The class is named: com.pingidentity.pa.sample.SampleRule and has ide generated default the method implementations, we'll revisit these later.

1. Examine the Services file.  It is already defined for you, but if you needed to create on,  you would create a file with the same name as the SPI interface like the following: 
 
	    <GITREPO>/src/main/resources/META-INF/services/com.pingidentity.pa.sdk.policy.RuleInterceptor
    
    That file includes the name of the implementation class: *com.pingidentity.pa.sample.SampleRule*
   
1. The Rule also needs a Descriptor.  It should be annotated with the *@Rule* annotation (*com.pingidentity.pa.sdk.policy.Rule*).  
At a minimum, the *type* and *label* should be defined.  The *type* should be a unique String with no white space.  The *Label* 
 is the text label for the Rule.
 

    @Rule(type = "SampleRule", label = "SampleRule") 
    
1. Next let's start implementing methods and build the UI.  The easiest way to do build a class and use annotations on that
class to drive the UI as well as use it to bind to the configuration.  

1. Define a public static nested class that extends *com.pingidentity.pa.sdk.policy.SimplePluginConfiguration*.  
Since every Rule must include at least one UI field, let's add a public String field for the regex to match.

        public String validUserAgentRegex = null;

1. To make that field something that renders a UI element on the screen add a *com.pingidentity.pa.sdk.ui.UIElement* annotation.  
The *label* defines the UI label for this field.  The *type* determines what sort of UI field will be rendered. 
The *order* defines the order of the UI Elements on the screen, since there's only one field, 0 is a perfectly fine value.

        @UIElement(label = "A Valid User Agent",
                type = ConfigurationType.TEXT,
                order=0,
                help = @Help(title = "This is a Regex for the allowable User Agents")
        )

1. Since we need this value to be defined and valid, we use constraints from *javax.validation* to validate the field.
    Annotate with javax.validation.constraints.NotNull and the included annotation  *com.pingidentity.pa.sample.Regex*.
    
            @NotNull(message="Please provide a Regex")
            @Regex(message="Not a valid Regex")
    
1. Next, we need to compile and the cache the regex pattern.  In the *Configuration* class, let's define a member to cache the Pattern and 
    a method to initialize and acquire it.  (We will call this method later and at a particular time in the life cycle.)
    
            Pattern pattern;
    
            public Pattern getPattern()  {
                if (pattern == null ) {
                    pattern = Pattern.compile(validUserAgentRegex);
                }
                return pattern;
            }

1.  The way we communicate the UI to PingAccess is by implementing the *public List<ConfigurationField> getConfigurationFields()* on the Rule.
    There are a couple ways to build the list of ConfigurationFields, but the easiest and preferred is to use the fluent interface provided by *ConfigurationBuilder* 
    to extract the list from the Annotated fields defined on the *Configuration* like so: 
         
         @Override
         public List<ConfigurationField> getConfigurationFields() {
            return ConfigurationBuilder
                .from(Configuration.class)
                .toConfigurationFields();
         }
    
1. In addition to describing the UI we need to accept configuration from the UI.  The first step is to tell the framework
that you expect to be passed the *Configuration* object.  This is neccessary because PA will try to map the information 
provided by the UI--which arrives in the form of JSON at the Rules REST service--to whatever class you tell it to use.
We simply override the default *expectedConfiguration* attribute on the *Rule* annotation.
    
        @Rule(type = "SampleRule", label = "SampleRule" , expectedConfiguration = SampleRule.Configuration.class)
        
1.  To eliminate casting, we should parameterize the Rule based on the expectedConfiguration.
     
        public class SampleRule extends RuleInterceptorBase<SampleRule.Configuration>
        
        
1. Now PA will render the UI with the *Configuration* class and provide the Rule with a valid instance (assuming the user 
    submits valid data.)  Since we extended *RuleInterceptorBase* we have a default implementation of the *configure* and
    *getConfiguration* methods.  We're going to to override the *configure* method to take advantage of the fact that it 
    is called exactly once before a given Rule is made available to handle requests.  This gives us a handy place to
    call the getPattern() method on the *Configuration* object and initialize it, thereby eliminating the need to synchronize
    around the pattern compilation to avoid race conditions. (Note that this Rule may be instantiated many times, but any given
    instance's Configure method will only be called once.)
    
        @Override
        public void configure(Configuration pluginConfiguration) throws ValidationException {
            super.configure(pluginConfiguration);
            getConfiguration().getPattern();
        }
        
1. Rules may be called upon to perform Error Handling.  This usually happens when one of the Rules being evaluated for a 
    request--not necessarily this rule--throws an AccessException from its HandleRequest method.  We're choosing a simple 
    implementation here.
    
        @Override
        public ErrorHandlingCallback getErrorHandlingCallback() {
            return new InternalServerErrorCallback();
        }
        
1. We're almost ready to perform the actual *handleRequest* method.  First let's create a SLF4J logger to use for debugging.
    
        Logger log = LoggerFactory.getLogger(SampleRule.class);
        
1. Now we can implement HandleRequest.  We need to get the Header from the exchange and get the value of any "User-Agent" 
HeaderField if it is present.  There should only be one value, so we get the last value.  Next we build a matcher from the
compiled Pattern and see if the provided "User-Agent" header matches the provided pattern.  And we throw in a bunch of logging
so we can see what's going on.
    
        @Override
        public Outcome handleRequest(Exchange exchange) throws RuntimeException, IOException, InterruptedException {
            log.debug("Begin Handling Request");
    
            String userAgent = exchange.getRequest().getHeader().getLastValue("User-Agent");
    
            if (userAgent != null && getConfiguration().getPattern().matcher(userAgent).matches()) {
                log.debug( "Found a valid user agent {} ", userAgent );
            } else {
                log.debug( "Found an invalid user agent {} ", userAgent );
                throw new AccessException("No UserAgent Found.");
            }
    
            log.debug("Done Handling Request");
            return Outcome.CONTINUE;
        }
        
1. We're finished with the code.  Compile the rule from the folder with the pom in it:

        $ mvn package
 
1. Copy the resultant jar from the target directory into the PingAccess *lib* folder and start PingAccess.  It's ready to use.
        


## Appendix

### (has-no-tubes) No Internet Access

If internet access is unavailable, there are two other ways to reference the 
PingAccess SDK.  First, once Apache Maven is installed, install the sdk into
your local dependency repository by running the following command:  

Alternatively, you can update the pingaccess-sdk dependancy to point to
the local installation.

    <dependency>
        <groupId>com.pingidentity.pingaccess</groupId>
        <artifactId>pingaccess-sdk</artifactId>
        <version>3.0.0</version>
        <scope>system</scope>
        <systemPath><PATH_TO_PA_INSTALL>/lib/pingaccess-sdk-3.0.jar</systemPath>
    </dependency>

With either of these options, replace <PATH_TO_PA_INSTALL> with the path to the 
pa installation.

### <a name="java"></a> Java

http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html

### <a name="git"></a> git

You may have used git to get access to this.  Otherwise, you can find the download here:

git is available [here](http://git-scm.com/downloads)


### <a name="apache-maven></a> Apache Maven

The sample uses Apache Maven and assume that the PingAccess SDK can be
referenced as a dependency.  They reference PingIdentity's public maven
repository, located at: 

http://maven.pingidentity.com/release

Apache Maven [is available here][maven]


### Apache Maven

[maven]: http://maven.apache.org/download.html
[CIS2014]: http://www.cloudidentitysummit.com/events/cloud-identity-summit-2014/event-summary-e09252dca9f144cbbd77691980893949.aspx
[userAgent]:	http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43

