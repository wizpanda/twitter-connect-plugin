# twitter-connect-plugin
Cordova/PhoneGap plugin to use Twitter Single Sign On

Using Twitter's Fabric SDK, you can enable SSO with your Android apps. It's a fairly involved process, so I'll try to lay out every step necessary.

### Install

##### Get a Fabric API key
To use Fabric, you'll need to [sign up](https://get.fabric.io/twitter-login). Apparently they have to authorize people manually, so it might be a while until your account is created, mine took about an hour.

The only thing we really need is the API key. You can find it on the [organizations page](https://fabric.io/settings/organizations/). Click or create your organization and under the header is a link for API Key.

##### Create a Twitter app
Create a Twitter application and get the consumer key and consumer secret.

##### Add plugin to your Cordova app
`cordova plugin add https://github.com/ManifestWebDesign/twitter-connect-plugin.git --variable FABRIC_KEY=<Fabric API Key>`

Make sure you put in your valid Fabric API Key.

##### Add configuration to config.xml
Open `config.xml` (in your project's root) and add these two lines before the closing ```</widget>``` tag:
````
<preference name="TwitterConsumerKey" value="<Twitter Consumer Key>" />
<preference name="TwitterConsumerSecret" value="<Twitter Consumer Secret>" />
````
Of course, replace the values with the keys you got from the above steps.

##### Modify `build.gradle`
Unfortunately, I don't know of an easier way to do this. This part can be overwritten if you're not careful.

Open the file `platforms/android/build.gradle`

Underneath the first `buildscript` heading (around line 24), change the `repositories` block to this:
```
repositories {
  mavenCentral()
  jcenter()
  maven { url 'https://maven.fabric.io/public' }
}
```

Next, go down to around line 48. You should be right after the big `if (gradle.gradleVersion >= "2.2") {` closing brace.

Add this:
```
dependencies {
  classpath 'com.android.tools.build:gradle:1.1.3'
  classpath 'io.fabric.tools:gradle:1.+'  
}
```

##### Finished!
You should now be able to: `cordova run android`

### Usage

This plugin adds an object to the window named TwitterConnect. Right now, you can only login and logout.

##### Login

Login using the `.login` method:
```
TwitterConnect.login(
  function(result) {
    console.log('Successful login!');
    console.log(result);
  }, function(error) {
    console.log('Error logging in');
    console.log(error);
  }
);
```

The login reponse oject is defined as:
```
{
  userName: '<Twitter User Name>',
  userId: '<Twitter User Id>',
  secret: '<Twitter Oauth Secret>',
  token: '<Twitter Oauth Token>'
}
```

##### Logout

Logout using the `.logout` method:
```
TwitterConnect.logout(
  function() {
	console.log('Successful logout!');
  },
  function() {
    console.log('Error logging out');
  }
);
```
