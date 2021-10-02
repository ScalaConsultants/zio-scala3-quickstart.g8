package $package$.api.graphql

object Queries:
  val getAll: String = """
      |query {
      |  getAllItems {
      |    id{
      |      value
      |    }
      |    , description
      |  }
      |}
      |""".stripMargin

  def getById(id: String): String = s"""
      |query{
      |  getItem(itemId:"\$id"){
      |    id{
      |      value
      |    }
      |    description
      |  }
      |}
      |""".stripMargin

object Mutations:
  def save(desc: String) = s"""
      |mutation {
      |  addItem(description: "\$desc"){
      |    value
      |  }
      |}
      |""".stripMargin

  def update(id: String, desc: String) = s"""
      |mutation{
      |  updateItem(itemId: "\$id", description: "\$desc")
      |}
      |""".stripMargin

  def delete(id: String) = s"""
      |mutation{
      |  deleteItem(itemId: "\$id")
      |}
      |""".stripMargin

object Subscriptions:
  val streamDeleted: String = """
      |subscription{
      |  deletedEventsStream {
      |    value
      |  }
      |}""".stripMargin
