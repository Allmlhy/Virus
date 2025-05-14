object KafkaConsumerMain {
  def main(args: Array[String]): Unit = {
    println("启动 test 与 test2 Topic 的合并消费者...")
    CombinedKafkaConsumer.start()
  }
}
