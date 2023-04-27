module.exports = {
    docs: {
        About: [
            'about/about_index',
            'about/about_contributing',
            'about/about_code_of_conduct'
        ],
        Overview: [
            'overview/overview_index',
            'overview/overview_installation',
            'overview/overview_usage',
            {
                type: 'category',
                label: 'Elastic Query',
                items: [
                    'overview/elastic_query',
                    'overview/queries/elastic_query_contains',
                    'overview/queries/elastic_query_exists',
                    'overview/queries/elastic_query_filter',
                    'overview/queries/elastic_query_has_child',
                    'overview/queries/elastic_query_has_parent',
                    'overview/queries/elastic_query_match_all',
                    'overview/queries/elastic_query_matches',
                    'overview/queries/elastic_query_match_phrase',
                    'overview/queries/elastic_query_must',
                    'overview/queries/elastic_query_must_not',
                    'overview/queries/elastic_query_nested',
                    'overview/queries/elastic_query_range',
                    'overview/queries/elastic_query_should',
                    'overview/queries/elastic_query_starts_with',
                    'overview/queries/elastic_query_term',
                    'overview/queries/elastic_query_terms',
                    'overview/queries/elastic_query_wildcard',
                ],
            },
            {
                type: 'category',
                label: 'Elastic Aggregation',
                items: [
                    'overview/elastic_aggregation',
                    'overview/aggregations/elastic_aggregation_terms',
                ],
            },
            {
                type: 'category',
                label: 'Elastic Request',
                items: [
                    'overview/elastic_request',
                    'overview/requests/elastic_request_aggregate',
                    'overview/requests/elastic_request_bulk',
                    'overview/requests/elastic_request_count',
                    'overview/requests/elastic_request_create',
                    'overview/requests/elastic_request_create_index',
                    'overview/requests/elastic_request_delete_by_id',
                    'overview/requests/elastic_request_delete_by_query',
                    'overview/requests/elastic_request_delete_index',
                    'overview/requests/elastic_request_exists',
                    'overview/requests/elastic_request_get_by_id',
                    'overview/requests/elastic_request_search',
                    'overview/requests/elastic_request_upsert',
                ],
            },
            'overview/overview_zio_prelude_schema',
            'overview/overview_elastic_executor',
            'overview/overview_fluent_api',
            'overview/overview_bulkable',
            'overview/overview_streaming',
        ],
    },
};
