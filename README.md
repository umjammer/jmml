[![Release](https://jitpack.io/v/umjammer/jmml.svg)](https://jitpack.io/#umjammer/jmml)
[![Java CI](https://github.com/umjammer/jmml/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/jmml/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/jmml/actions/workflows/codeql.yml/badge.svg)](https://github.com/umjammer/jmml/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)

Java MSN Messenger Library
--------------------------

1. What is this?
2. Writing a Client
3. Requirements
4. Compiling the source code
5. Credits
6. Plea from Author
7. License

1. WHAT IS THIS?

Java MSN Messenger Library (JMML) is designed to be an easy-to-use library for 
developing MSN Messenger clients.

2. WRITING A CLIENT

The library was written with client development in mind.

2.1 Signing into MSN

Grab an instance of the MessengerServerManager (it's a singleton), and ask it 
to sign in with a given set of credentials.

  MessengerServerManager msn = MessengerServerManager.getInstance();
  msn.signIn ("my_passport@hotmail.com", "my_password", ContactStatus.ONLINE);

2.2 Send a message

After signing into Messenger, you can send a message by specifying the user to 
send to, and the message to send.

  msn.sendMessage ("my_friend@hotmail.com", "Hey my_friend, you are cool!");

2.3 Receiving messages

Like the AWT toolkit, you create a listener and register it with the event 
generator.  In this case, the MessengerServerManager is the event generator, 
and you create an object that implements the MessengerClientListener interface.

  msn.addMessengerClientListener (new MessageClientAdapter() {
    public void incomingMessage (IncomingMessageEvent e) {
      System.out.println (e.getUserName() + " said: " + e.getMessage());
    }
  });

3. Requirements
The JMML requires JDK/JRE 1.3 or higher and Java Secure Socket Extension 1.0.3 or higher (this library is already in JMML package - see bin directory for files jnet.jar, jsse.jar, jcert.jar). If you're using JDK/JRE 1.4 theses files are not needed.

4. COMPILING THE SOURCE CODE
To compile JMML you'll need add jnet.jar, jsse.jar, jcert.jar files to the JDK's classpath. You can use the build.xml ANT script to compile using the Apache's ANT. The build.xml script file can be found in ant_build_scripts directory.

5. CREDITS

Tony Tang - Me. [e: jmml@sleek.hn.org, w: http://www.sfu.ca/~tonyt/]

Leidson Campos (www.planetamessenger.org) - Support to MSNP8, improvements in release 0.4 and bug fixes e. leidson@planetamessenger.org, w. http://www.planetamessenger.org]

Thanks to:

Cheryl Tiu - For love and affection. :D
Mike Mintz - Author of MSN IM Protocol [http://www.hypothetic.org/docs/msn/index.php]

6. PLEA FROM AUTHOR

Hi!  If you use this, please send me a note.  I'd really appreciate bug reports and such.  Thanks!

7. LICENSE

This software is released under GPL [http://www.gnu.org/copyleft/gpl.html].  
Have fun.  Any modifications you make with this source must be re-released back the community.