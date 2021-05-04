package com.nosto.elastijack

import com.dimafeng.testcontainers.{ElasticsearchContainer, ForAllTestContainer}
import com.nosto.{ElasticSearchJavaBeanMapper, ElasticsearchMapper}
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders.matchAllQuery
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.jeasy.random.{EasyRandom, EasyRandomParameters}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.util.UUID
import scala.reflect.ClassTag
import scala.util.Random.nextInt

class MainSuite extends AnyFunSuite with ForAllTestContainer {

  override val container: ElasticsearchContainer = ElasticsearchContainer()
  final val client = new RestHighLevelClient(
    RestClient.builder(
      new HttpHost(container.host, 9200, "http"),
      new HttpHost("localhost", 9201, "http")))

  final val randomParams = new EasyRandomParameters()
  final val easyRandom = new EasyRandom(randomParams)

  def sanityCheck[T: ClassTag](mapping: String)(implicit indexName: String = UUID.randomUUID().toString):
  Unit = {
    val mapper = new ElasticSearchJavaBeanMapper[TestJavaBean]()

    val createIndex: CreateIndexRequest = new CreateIndexRequest(indexName)
      .mapping(mapping, XContentType.JSON)
    client.indices().create(createIndex, RequestOptions.DEFAULT)

    //noinspection ScalaStyle
    val insertedObjects: Set[TestJavaBean] = Seq.fill(nextInt(5))(easyRandom.nextObject(classOf[TestJavaBean]))
      .map { (bean: TestJavaBean) =>
        val indexRequest: IndexRequest = new IndexRequest(indexName)
          .source(mapper.toJson(bean), XContentType.JSON)

        client.index(indexRequest, RequestOptions.DEFAULT)
        bean
      }.toSet

    val searchSourceBuilder = new SearchSourceBuilder()
      .query(matchAllQuery())
    val searchRequest = new SearchRequest()
      .source(searchSourceBuilder)

    val searchResponse: SearchResponse = client.search(searchRequest, RequestOptions.DEFAULT)
    val queriedObjects: Set[TestJavaBean] = searchResponse.getHits.getHits
      .map { hit: SearchHit => hit.getSourceAsString }
      .map { (json: String) =>
        mapper.fromJson(json)
      }.toSet

    insertedObjects shouldEqual queriedObjects
  }

  test("can generate ES index schema from Java Beans") {
    val mapper = new ElasticSearchJavaBeanMapper[TestJavaBean]()
    val mapping: String = mapper.generateMappingProperties
    mapping shouldEqual
      """{
        |  "properties" : {
        |    "date" : {
        |      "format" : "yyyy-MM-dd",
        |      "type" : "date"
        |    },
        |    "datetime" : {
        |      "type" : "date"
        |    },
        |    "foo" : {
        |      "type" : "keyword"
        |    }
        |  }
        |}""".stripMargin

    sanityCheck[TestJavaBean](mapping)
  }

  test("can generate ES index schema from Scala Beans") {
    val mapper = new ElasticsearchMapper[CaseClassWithGetters]()
    val mapping: String = mapper.generateMappingProperties
    println(mapping)

  }

  test("can generate ES index schema from Scala Beans1") {
    val mapper = new ElasticsearchMapper[ClassWithBeanProp]()
    val mapping: String = mapper.generateMappingProperties
    println("dfd")
    println(mapping)
    //Nothing

  }

  test("can generate ES index schema from Scala case classes with bean-property annotations") {
    val mapper = new ElasticsearchMapper[CaseClassWithBeanProperties]()
    val mapping: String = mapper.generateMappingProperties
    println("dddfd")
    println(mapping)
    //Nothing

  }
}
