"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[53],{1109:e=>{e.exports=JSON.parse('{"pluginId":"default","version":"current","label":"Next","banner":null,"badge":false,"noIndex":false,"className":"docs-version-current","isLast":true,"docsSidebars":{"docs":[{"type":"category","label":"About","items":[{"type":"link","label":"About ZIO Elasticsearch","href":"/zio-elasticsearch/about/","docId":"about/about_index"},{"type":"link","label":"Contributing","href":"/zio-elasticsearch/about/about_contributing","docId":"about/about_contributing"},{"type":"link","label":"Code of Conduct","href":"/zio-elasticsearch/about/about_code_of_conduct","docId":"about/about_code_of_conduct"}],"collapsed":true,"collapsible":true},{"type":"category","label":"Overview","items":[{"type":"link","label":"Summary","href":"/zio-elasticsearch/overview/","docId":"overview/overview_index"},{"type":"link","label":"Installation","href":"/zio-elasticsearch/overview/overview_installation","docId":"overview/overview_installation"},{"type":"link","label":"Usage","href":"/zio-elasticsearch/overview/overview_usage","docId":"overview/overview_usage"},{"type":"category","label":"Elastic Query","items":[{"type":"link","label":"Overview","href":"/zio-elasticsearch/overview/overview_elastic_query","docId":"overview/overview_elastic_query"},{"type":"link","label":"Contains Query","href":"/zio-elasticsearch/overview/queries/overview_elastic_query_contains","docId":"overview/queries/overview_elastic_query_contains"},{"type":"link","label":"Exists Query","href":"/zio-elasticsearch/overview/queries/overview_elastic_query_exists","docId":"overview/queries/overview_elastic_query_exists"}],"collapsed":true,"collapsible":true},{"type":"category","label":"Elastic Aggregation","items":[{"type":"link","label":"Overview","href":"/zio-elasticsearch/overview/overview_elastic_aggregation","docId":"overview/overview_elastic_aggregation"},{"type":"link","label":"Terms Aggregation","href":"/zio-elasticsearch/overview/aggregations/overview_elastic_aggregation_terms","docId":"overview/aggregations/overview_elastic_aggregation_terms"}],"collapsed":true,"collapsible":true},{"type":"category","label":"Elastic Request","items":[{"type":"link","label":"Overview","href":"/zio-elasticsearch/overview/overview_elastic_request","docId":"overview/overview_elastic_request"},{"type":"link","label":"Aggregation Request","href":"/zio-elasticsearch/overview/requests/overview_elastic_request_aggregate","docId":"overview/requests/overview_elastic_request_aggregate"},{"type":"link","label":"Bulk Request","href":"/zio-elasticsearch/overview/requests/overview_elastic_request_bulk","docId":"overview/requests/overview_elastic_request_bulk"},{"type":"link","label":"Count Request","href":"/zio-elasticsearch/overview/requests/overview_elastic_request_count","docId":"overview/requests/overview_elastic_request_count"}],"collapsed":true,"collapsible":true},{"type":"link","label":"Use of ZIO Prelude and Schema","href":"/zio-elasticsearch/overview/overview_zio_prelude_schema","docId":"overview/overview_zio_prelude_schema"},{"type":"link","label":"Executing Requests","href":"/zio-elasticsearch/overview/overview_elastic_executor","docId":"overview/overview_elastic_executor"},{"type":"link","label":"Fluent API","href":"/zio-elasticsearch/overview/overview_fluent_api","docId":"overview/overview_fluent_api"},{"type":"link","label":"Bulkable","href":"/zio-elasticsearch/overview/overview_bulkable","docId":"overview/overview_bulkable"},{"type":"link","label":"Streaming","href":"/zio-elasticsearch/overview/overview_streaming","docId":"overview/overview_streaming"}],"collapsed":true,"collapsible":true}]},"docs":{"about/about_code_of_conduct":{"id":"about/about_code_of_conduct","title":"Code of Conduct","description":"Our Pledge","sidebar":"docs"},"about/about_contributing":{"id":"about/about_contributing","title":"Contributing","description":"We welcome contributions from anybody wishing to participate. All code or documentation that is provided must be licensed under Apache 2.0.","sidebar":"docs"},"about/about_index":{"id":"about/about_index","title":"About ZIO Elasticsearch","description":"ZIO Elasticsearch is a type-safe and streaming-friendly ZIO-native Elasticsearch client.","sidebar":"docs"},"overview/aggregations/overview_elastic_aggregation_terms":{"id":"overview/aggregations/overview_elastic_aggregation_terms","title":"Terms Aggregation","description":"TBD","sidebar":"docs"},"overview/overview_bulkable":{"id":"overview/overview_bulkable","title":"Bulkable","description":"If you want to use Elasticsearch\'s Bulk API you can do so using the bulk method.","sidebar":"docs"},"overview/overview_elastic_aggregation":{"id":"overview/overview_elastic_aggregation","title":"Overview","description":"In order to execute Elasticsearch aggregation requests...","sidebar":"docs"},"overview/overview_elastic_executor":{"id":"overview/overview_elastic_executor","title":"Executing Requests","description":"In order to get the functional effect of executing a specified Elasticsearch request, you should call the execute method defined in the Elasticsearch, which returns a ZIO that requires an Elasticsearch, fails with a Throwable and returns the relevant value A for that request.","sidebar":"docs"},"overview/overview_elastic_query":{"id":"overview/overview_elastic_query","title":"Overview","description":"In order to execute Elasticsearch query requests, both for searching and deleting by query,","sidebar":"docs"},"overview/overview_elastic_request":{"id":"overview/overview_elastic_request","title":"Overview","description":"We can represent an Elasticsearch request as a generic data type ElasticRequest[A], where A represents the result of the executed request.","sidebar":"docs"},"overview/overview_fluent_api":{"id":"overview/overview_fluent_api","title":"Fluent API","description":"Both Elastic requests and queries offer a fluent API so that we could provide optional parameters in chained method calls for each request or query.","sidebar":"docs"},"overview/overview_index":{"id":"overview/overview_index","title":"Summary","description":"scala-version","sidebar":"docs"},"overview/overview_installation":{"id":"overview/overview_installation","title":"Installation","description":"To use ZIO Elasticsearch in your project, add the following to your build.sbt file:","sidebar":"docs"},"overview/overview_streaming":{"id":"overview/overview_streaming","title":"Streaming","description":"ZIO Elasticsearch offers a few different API methods for creating ZIO streams out of search requests.","sidebar":"docs"},"overview/overview_usage":{"id":"overview/overview_usage","title":"Usage","description":"In order to execute an Elasticsearch request we can rely on the Elasticsearch layer which offers an execute method accepting an ElasticRequest. In order to build the Elasticsearch layer we need to provide the following layers:","sidebar":"docs"},"overview/overview_zio_prelude_schema":{"id":"overview/overview_zio_prelude_schema","title":"Use of ZIO Prelude and Schema","description":"ZIO Prelude is a library focused on providing a core set of functional data types and abstractions that can help you solve a variety of day-to-day problems.","sidebar":"docs"},"overview/queries/overview_elastic_query_contains":{"id":"overview/queries/overview_elastic_query_contains","title":"Contains Query","description":"TBD","sidebar":"docs"},"overview/queries/overview_elastic_query_exists":{"id":"overview/queries/overview_elastic_query_exists","title":"Exists Query","description":"TBD","sidebar":"docs"},"overview/requests/overview_elastic_request_aggregate":{"id":"overview/requests/overview_elastic_request_aggregate","title":"Aggregation Request","description":"TBD","sidebar":"docs"},"overview/requests/overview_elastic_request_bulk":{"id":"overview/requests/overview_elastic_request_bulk","title":"Bulk Request","description":"TBD","sidebar":"docs"},"overview/requests/overview_elastic_request_count":{"id":"overview/requests/overview_elastic_request_count","title":"Count Request","description":"TBD","sidebar":"docs"}}}')}}]);