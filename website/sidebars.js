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
