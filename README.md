### To build and run the Test example
1. Build the assembly ```./sbt/sbt package```
1. Submit the assembly to the spark server ```dse spark-submit --class TestConsumer ./target/scala-2.10/chunking-assembly-0.2.jar```