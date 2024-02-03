"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[788],{7856:(e,o,n)=>{n.r(o),n.d(o,{assets:()=>l,contentTitle:()=>s,default:()=>u,frontMatter:()=>r,metadata:()=>a,toc:()=>c});var i=n(7624),t=n(2172);const r={id:"elastic_query_geo_polygon",title:"Geo-polygon Query"},s=void 0,a={id:"overview/queries/elastic_query_geo_polygon",title:"Geo-polygon Query",description:"A query returning hits that only fall within a polygon of points.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_geo_polygon.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_geo_polygon",permalink:"/zio-elasticsearch/overview/queries/elastic_query_geo_polygon",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_geo_polygon.md",tags:[],version:"current",frontMatter:{id:"elastic_query_geo_polygon",title:"Geo-polygon Query"},sidebar:"docs",previous:{title:"Geo-distance Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_geo_distance"},next:{title:"Has Child Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_has_child"}},l={},c=[];function d(e){const o=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,t.M)(),e.components);return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(o.p,{children:"A query returning hits that only fall within a polygon of points."}),"\n",(0,i.jsxs)(o.p,{children:["In order to use the ",(0,i.jsx)(o.code,{children:"GeoPolygon"})," query import the following:"]}),"\n",(0,i.jsx)(o.pre,{children:(0,i.jsx)(o.code,{className:"language-scala",children:"import zio.elasticsearch.query.GeoPolygonQuery\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,i.jsxs)(o.p,{children:["You can create a ",(0,i.jsx)(o.code,{children:"GeoPolygon"})," query using the ",(0,i.jsx)(o.code,{children:"geoPolygon"})," method with list of coordinates in the following manner:"]}),"\n",(0,i.jsx)(o.pre,{children:(0,i.jsx)(o.code,{className:"language-scala",children:'val query: GeoPolygonQuery = geoPolygon(field = "location", List("0, 0", "0, 90", "90, 90", "90, 0"))\n'})}),"\n",(0,i.jsxs)(o.p,{children:["You can create a ",(0,i.jsx)(o.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,i.jsx)(o.code,{children:"GeoPolygon"})," query using the ",(0,i.jsx)(o.code,{children:"geoPolygon"})," method with list of coordinates in the following manner:"]}),"\n",(0,i.jsx)(o.pre,{children:(0,i.jsx)(o.code,{className:"language-scala",children:'val query: GeoPolygonQuery = geoPolygon(field = Document.location, List("0, 0", "0, 90", "90, 90", "90, 0"))\n'})}),"\n",(0,i.jsxs)(o.p,{children:["If you want to change the ",(0,i.jsx)(o.code,{children:"_name"}),", you can use ",(0,i.jsx)(o.code,{children:"name"})," method:"]}),"\n",(0,i.jsx)(o.pre,{children:(0,i.jsx)(o.code,{className:"language-scala",children:'val queryWithName: GeoPolygonQuery = geoPolygon(field = "location", coordinates = List("0, 0", "0, 90", "90, 90", "90, 0")).name("name")\n'})}),"\n",(0,i.jsxs)(o.p,{children:["If you want to change the ",(0,i.jsx)(o.code,{children:"validation_method"}),", you can use ",(0,i.jsx)(o.code,{children:"validationMethod"})," method:"]}),"\n",(0,i.jsx)(o.pre,{children:(0,i.jsx)(o.code,{className:"language-scala",children:'import zio.elasticsearch.query.ValidationMethod\n\nval queryWithValidationMethod: GeoPolygonQuery = geoPolygon(field = "location", coordinates =  List("0, 0", "0, 90", "90, 90", "90, 0")).validationMethod(value = ValidationMethod.IgnoreMalformed)\n'})}),"\n",(0,i.jsxs)(o.p,{children:["You can find more information about ",(0,i.jsx)(o.code,{children:"GeoPolygon"})," query ",(0,i.jsx)(o.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-polygon-query.html",children:"here"}),"."]})]})}const u=function(e={}){const{wrapper:o}=Object.assign({},(0,t.M)(),e.components);return o?(0,i.jsx)(o,Object.assign({},e,{children:(0,i.jsx)(d,e)})):d(e)}},2172:(e,o,n)=>{n.d(o,{M:()=>s});var i=n(1504);const t={},r=i.createContext(t);function s(e){const o=i.useContext(r);return i.useMemo((function(){return"function"==typeof e?e(o):{...o,...e}}),[o,e])}}}]);