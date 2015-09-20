# SpraySlickSwagger
The Spray Slick Swagger is a very simple rest api in [Spray](https://github.com/spray/spray) showing one way of using spray with [slick 3](https://github.com/slick/slick) library for database access, and [spray-swagger](https://github.com/gettyimages/spray-swagger) for documentation the routes in spray.


It supports the following features:

* Spray-json to parse json
* Swagger to document routes and json
* Data access layer composed by data access Akka actors.
* Models as case classes and slick models, independent from database driver and profile (Go slick3)
* Multiple database types configured in properties file (h2 and postgresql for instance)
* Cake pattern for DI
* Routes Tested using specs2
* Data Access Actor Tested using scalatest

Utils: 

* Typesafe config for property management
* Typesafe Scala Logging (LazyLogging)

The project was thought to be used as a template for creating spray projects with this features. Soon, I'll make it an activator project.

#Running

The database pre-configured is an h2, so you just have to:

1. Launch SBT:

        $ sbt

2. Compile everything and run:

        > run

#Testing

1. Launch SBT:

        $ sbt

2. Compile everything and test:

        > test


#TODO

Make this project an activator template.

#Credits

I believe that this combination is one of the greatest ways to make a scalable api. To make this template, I just mixed the tutorials and templates from spray and slick, so credits for spray and slick. And for the really great work from gettyimages in making the swagger magic possible in spray.
# akka-fiddling
