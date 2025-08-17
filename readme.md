Hello

First please install sbt, and if it asks you to choose a java version, java 21 is the latest supported version of java for this framework. 
If you use intellij, you will probably have the easiest setup experience.

Edit your name and org in `build.sbt`, and also generate a secret key and put it in for `application.conf`. A secret key can be generated from the sbt command line with `playGenerateSecret`.

then do `sbt run` to start the server

then visit http://localhost:9000/ to view the Neolog.

Currently, the database of neologisms and corpus of all english words for the Tonitrus system are located at the root directory of the repository.
There are several hardcoded filepaths in the deploy scripts you may want to modify for your particular installation.
