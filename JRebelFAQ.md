For a detailed FAQ, refer to an [official documentation](http://zeroturnaround.com/software/jrebel/resources/).


# Why should I use it? #

Because it does actually (hopefully) speed up the code-deploy-test cycle. It decreases turnaround by instantly reloading changes to your code after the compilation, without having to restart the container or redeploy the application, and does it in a matter of milliseconds.


# How does it work? #

There is some JVM magic including Java agent and bytecode instrumentation inside. For instance, this article covers some of this topic:

[Reloading Java Classes: HotSwap and JRebel — Behind the Scenes](http://zeroturnaround.com/jrebel/reloading_java_classes_401_hotswap_jrebel/)


# How to install? #

A detailed guide for Intellij is [here](http://zeroturnaround.com/software/jrebel/download/using-jrebel-with-intellij/). Actually, it's enough to install the IDEA plug-in (by coincidence called 'JRebel') with no additional downloads required.

NetBeans is also supported, the guide is [here](http://zeroturnaround.com/software/jrebel/download/installing-jrebel-for-netbeans/).

## Licensing ##

JRebel is not free, but there's a "convenient patch" on [RuTracker](http://rutracker.org/forum/viewtopic.php?t=3384501). You can also request a 14-day trial.

As for an aforementioned patch and IDEA, copy _jrebel.jar_ to _%USERPROFILE%/.IntelliJIdea12/config/plugins/jr-ide-idea/lib/jrebel_ and restart IDEA.


# Usage in IDEA #

  1. Use IDEA >= 12 because of a [new compilation mode](http://blogs.jetbrains.com/idea/2012/06/brand-new-compiler-mode-in-intellij-idea-12-leda).
  1. IDE plug-in adds run configurations called 'Run application with JRebel agent' and 'Debug application with JRebel agent'.
  1. Current setup of JRebel config requires environment variables to be specified in the JVM options. See [rebel.xml](http://code.google.com/p/osznconnection/source/browse/trunk/osznconnection-web/src/main/resources/rebel.xml) for a list of those.
  1. If something goes wrong, add _-Drebel.log=true_ option to the JVM command-line and see _%USERPROFILE%/.IntelliJIdea12/config/plugins/jr-ide-idea/lib/jrebel/jrebel.log_ for the debugging output.