"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[3909],{5143:(e,r,t)=>{t.r(r),t.d(r,{assets:()=>o,contentTitle:()=>n,default:()=>d,frontMatter:()=>i,metadata:()=>s,toc:()=>c});var a=t(5893),g=t(1151);const i={id:"elastic_aggregation_terms",title:"Terms Aggregation"},n=void 0,s={unversionedId:"overview/aggregations/elastic_aggregation_terms",id:"overview/aggregations/elastic_aggregation_terms",title:"Terms Aggregation",description:"This aggregation is a multi-bucket value source based aggregation where buckets are dynamically built - one per unique value.",source:"@site/../modules/docs/target/mdoc/overview/aggregations/elastic_aggregation_terms.md",sourceDirName:"overview/aggregations",slug:"/overview/aggregations/elastic_aggregation_terms",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_terms",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/aggregations/elastic_aggregation_terms.md",tags:[],version:"current",frontMatter:{id:"elastic_aggregation_terms",title:"Terms Aggregation"},sidebar:"docs",previous:{title:"Max Aggregation",permalink:"/zio-elasticsearch/overview/aggregations/elastic_aggregation_max"},next:{title:"Overview",permalink:"/zio-elasticsearch/overview/elastic_request"}},o={},c=[];function l(e){const r=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,g.ah)(),e.components);return(0,a.jsxs)(a.Fragment,{children:[(0,a.jsx)(r.p,{children:"This aggregation is a multi-bucket value source based aggregation where buckets are dynamically built - one per unique value."}),"\n",(0,a.jsxs)(r.p,{children:["In order to use the ",(0,a.jsx)(r.code,{children:"Terms"})," aggregation import the following:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:"import zio.elasticsearch.aggregation.TermsAggregation\nimport zio.elasticsearch.ElasticAggregation.termsAggregation\n"})}),"\n",(0,a.jsxs)(r.p,{children:["You can create a ",(0,a.jsx)(r.code,{children:"Terms"})," aggregation using the ",(0,a.jsx)(r.code,{children:"termsAggregation"})," method this way:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:'val aggregation: TermsAggregation = termsAggregation(name = "termsAggregation", field = "stringField.keyword")\n'})}),"\n",(0,a.jsxs)(r.p,{children:["You can create a ",(0,a.jsx)(r.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,a.jsx)(r.code,{children:"Terms"})," aggregation using the ",(0,a.jsx)(r.code,{children:"termsAggregation"})," method this way:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:'// Document.stringField must be string value, because of Terms aggregation\nval aggregation: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField.keyword)\n'})}),"\n",(0,a.jsxs)(r.p,{children:["If you want to change the ",(0,a.jsx)(r.code,{children:"order"}),", you can use ",(0,a.jsx)(r.code,{children:"orderBy"}),", ",(0,a.jsx)(r.code,{children:"orderByCountAsc"}),", ",(0,a.jsx)(r.code,{children:"orderByCountDesc"}),", ",(0,a.jsx)(r.code,{children:"orderByKeyAsc"})," or ",(0,a.jsx)(r.code,{children:"orderByKeyDesc"})," method:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:'import zio.elasticsearch.aggregation.AggregationOrder\nimport zio.elasticsearch.query.sort.SortOrder.Asc\n\nval aggregationWithOrder1: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderBy(AggregationOrder("otherAggregation", Asc))\nval aggregationWithOrder2: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderByCountAsc\nval aggregationWithOrder3: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderByCountDesc\nval aggregationWithOrder4: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderByKeyAsc\nval aggregationWithOrder5: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderByKeyDesc\n'})}),"\n",(0,a.jsxs)(r.p,{children:["If you want to change the ",(0,a.jsx)(r.code,{children:"size"}),", you can use ",(0,a.jsx)(r.code,{children:"size"})," method:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:'val aggregationWithSize: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).size(5)\n'})}),"\n",(0,a.jsxs)(r.p,{children:["If you want to add aggregation (on the same level), you can use ",(0,a.jsx)(r.code,{children:"withAgg"})," method:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:'val multipleAggregations: MultipleAggregations = termsAggregation(name = "termsAggregation", field = Document.stringField).withAgg(maxAggregation(name = "maxAggregation", field = Document.intField))\n'})}),"\n",(0,a.jsxs)(r.p,{children:["If you want to add another sub-aggregation, you can use ",(0,a.jsx)(r.code,{children:"withSubAgg"})," method:"]}),"\n",(0,a.jsx)(r.pre,{children:(0,a.jsx)(r.code,{className:"language-scala",children:'val aggregationWithSubAgg: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).withSubAgg(maxAggregation(name = "maxAggregation", field = Document.intField))\n'})}),"\n",(0,a.jsxs)(r.p,{children:["You can find more information about ",(0,a.jsx)(r.code,{children:"Terms"})," aggregation ",(0,a.jsx)(r.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-bucket-terms-aggregation.html#search-aggregations-bucket-terms-aggregation",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:r}=Object.assign({},(0,g.ah)(),e.components);return r?(0,a.jsx)(r,Object.assign({},e,{children:(0,a.jsx)(l,e)})):l(e)}},1151:(e,r,t)=>{t.d(r,{Zo:()=>s,ah:()=>i});var a=t(7294);const g=a.createContext({});function i(e){const r=a.useContext(g);return a.useMemo((()=>"function"==typeof e?e(r):{...r,...e}),[r,e])}const n={};function s({components:e,children:r,disableParentContext:t}){let s;return s=t?"function"==typeof e?e({}):e||n:i(e),a.createElement(g.Provider,{value:s},r)}}}]);