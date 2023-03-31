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
                    'overview/overview_elastic_query',
                    'overview/queries/overview_elastic_query_contains',
                    'overview/queries/overview_elastic_query_exists',
                ],
            },
            {
                type: 'category',
                label: 'Elastic Aggregation',
                items: [
                    'overview/overview_elastic_aggregation',
                    'overview/aggregations/overview_elastic_aggregation_terms',
                ],
            },
            {
                type: 'category',
                label: 'Elastic Request',
                items: [
                    'overview/overview_elastic_request',
                    'overview/requests/overview_elastic_request_aggregate',
                    'overview/requests/overview_elastic_request_bulk',
                    'overview/requests/overview_elastic_request_count',
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
