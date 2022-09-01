
const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

const config = {
  title: 'ZIO Elasticsearch',
  tagline: 'Elasticsearch client for ZIO',
  url: 'https://lambdaworks.github.io',
  baseUrl: '/zio-elasticsearch/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.png',
  organizationName: 'lambdaworks',
  projectName: 'zio-elasticsearch',
  i18n: {
    defaultLocale: 'en',
    locales: ['en']
  },
  presets: [
    [
      'classic',
      ({
        docs: {
          path: '../modules/zio-elasticsearch-docs/target/mdoc',
          routeBasePath: '/',
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl:
            'https://github.com/lambdaworks/zio-elasticsearch/edit/main/website/',
        },
        blog: false,
        theme: {
          customCss: require.resolve('./src/css/custom.css')
        }
      })
    ]
  ],
  themeConfig:
    ({
      navbar: {
        title: 'ZIO Elasticsearch',
        logo: {
          alt: 'lambdaworks',
          src: 'img/logo.svg',
          srcDark: 'img/logo-dark.svg',
        },
        items: [
          {
            type: 'doc',
            docId: 'overview/overview_index',
            position: 'right',
            label: 'Overview'
          },
          {
            type: 'doc',
            docId: 'about/about_index',
            position: 'right',
            label: 'About'
          },
          {
            href: 'https://github.com/lambdaworks/zio-elasticsearch',
            label: 'GitHub',
            position: 'right'
          }
        ]
      },
      footer: {
        style: 'dark',
        logo: {
          alt: 'lambdaworks',
          src: 'img/logo-footer.svg',
          href: 'https://www.lambdaworks.io/'
        },
        links: [
          {  
            title: 'GitHub',
            items: [
              {
                html: `
                <a href="https://github.com/lambdaworks/zio-elasticsearch">
                  <img src="https://img.shields.io/github/stars/lambdaworks/zio-elasticsearch?style=social" alt="github" />
                </a>
              `
              }
            ]
          },
          {
            title: 'Additional resources',
            items: [
              {
                label: 'Scaladoc of ZIO Elasticsearch',
                to: 'pathname:///zio-elasticsearch/api/'
              }
            ]
          }
        ],
        copyright: `Copyright © ${new Date().getFullYear()} LambdaWorks d.o.o.`
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
	    additionalLanguages: ['java', 'scala']
      }
    })
};

module.exports = config;
