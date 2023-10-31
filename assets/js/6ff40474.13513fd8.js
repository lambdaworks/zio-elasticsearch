"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[9084],{2818:(e,n,c)=>{c.r(n),c.d(n,{assets:()=>s,contentTitle:()=>t,default:()=>d,frontMatter:()=>r,metadata:()=>a,toc:()=>l});var o=c(5893),i=c(1151);const r={id:"elastic_query_function_score",title:"Function Score Query"},t=void 0,a={id:"overview/queries/elastic_query_function_score",title:"Function Score Query",description:"The FunctionScore allows you to modify the score of documents that are retrieved by a query.",source:"@site/../modules/docs/target/mdoc/overview/queries/elastic_query_function_score.md",sourceDirName:"overview/queries",slug:"/overview/queries/elastic_query_function_score",permalink:"/zio-elasticsearch/overview/queries/elastic_query_function_score",draft:!1,unlisted:!1,editUrl:"https://github.com/lambdaworks/zio-elasticsearch/edit/main/docs/overview/queries/elastic_query_function_score.md",tags:[],version:"current",frontMatter:{id:"elastic_query_function_score",title:"Function Score Query"},sidebar:"docs",previous:{title:"Exists Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_exists"},next:{title:"Fuzzy Query",permalink:"/zio-elasticsearch/overview/queries/elastic_query_fuzzy"}},s={},l=[];function u(e){const n=Object.assign({p:"p",code:"code",pre:"pre",a:"a"},(0,i.a)(),e.components);return(0,o.jsxs)(o.Fragment,{children:[(0,o.jsxs)(n.p,{children:["The ",(0,o.jsx)(n.code,{children:"FunctionScore"})," allows you to modify the score of documents that are retrieved by a query."]}),"\n",(0,o.jsxs)(n.p,{children:["In order to use the ",(0,o.jsx)(n.code,{children:"FunctionScore"})," query and create needed ",(0,o.jsx)(n.code,{children:"FunctionScoreFunction"})," import the following:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"import zio.elasticsearch.query.FunctionScoreQuery\nimport zio.elasticsearch.query.FunctionScoreFunction._\nimport zio.elasticsearch.ElasticQuery._\n"})}),"\n",(0,o.jsxs)(n.p,{children:["For creating ",(0,o.jsx)(n.code,{children:"FunctionScore"})," query you require ",(0,o.jsx)(n.code,{children:"FunctionScoreFunction"})," or multiple of them.\nYou can create these functions in following way."]}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["You can create ",(0,o.jsx)(n.code,{children:"DecayFunction"})," with ",(0,o.jsx)(n.code,{children:"DecayFunctionType.Exp"})," using the ",(0,o.jsx)(n.code,{children:"expDecayFunction"})," method with origin and scale in the following manner:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val function: DecayFunction[Any] = expDecayFunction("field", origin = "11, 12", scale = "2km")\n'})}),"\n",(0,o.jsxs)(n.p,{children:["You can create a ",(0,o.jsx)(n.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"}),"\n",(0,o.jsx)(n.code,{children:"DecayFunction"})," with ",(0,o.jsx)(n.code,{children:"DecayFunctionType.Exp"})," using the ",(0,o.jsx)(n.code,{children:"expDecayFunction"})," method with origin and scale in the following manner:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val function: DecayFunction[Document] = expDecayFunction(field = Document.field, origin = "11, 12", scale = "2km")\n'})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["You can create ",(0,o.jsx)(n.code,{children:"DecayFunction"})," with ",(0,o.jsx)(n.code,{children:"DecayFunctionType.Gauss"})," using the ",(0,o.jsx)(n.code,{children:"gaussDecayFunction"})," method with origin and scale in the following manner:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val function: DecayFunction[Any] = gaussDecayFunction(field = "field", origin = "11, 12", scale = "2km")\n'})}),"\n",(0,o.jsxs)(n.p,{children:["You can create a ",(0,o.jsx)(n.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"}),"\n",(0,o.jsx)(n.code,{children:"DecayFunction"})," with ",(0,o.jsx)(n.code,{children:"DecayFunctionType.Gauss"})," using the ",(0,o.jsx)(n.code,{children:"gaussDecayFunction"})," method with origin and scale in the following manner:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val function: DecayFunction[Document] = gaussDecayFunction(field = Document.field, origin = "11, 12", scale = "2km")\n'})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["You can create ",(0,o.jsx)(n.code,{children:"DecayFunction"})," with ",(0,o.jsx)(n.code,{children:"DecayFunctionType.Linear"})," using the ",(0,o.jsx)(n.code,{children:"linearDecayFunction"})," method with origin and scale in the following manner:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val function: DecayFunction[Any] = linearDecayFunction(field = "field", origin = "11, 12", scale = "2km")\n'})}),"\n",(0,o.jsxs)(n.p,{children:["You can create a ",(0,o.jsx)(n.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"}),"\n",(0,o.jsx)(n.code,{children:"DecayFunction"})," with ",(0,o.jsx)(n.code,{children:"DecayFunctionType.Linear"})," using the ",(0,o.jsx)(n.code,{children:"expDecayFunction"})," method with origin and scale in the following manner:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val function: DecayFunction[Document] = linearDecayFunction(field = Document.field, origin = "11, 12", scale = "2km")\n'})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["You can create ",(0,o.jsx)(n.code,{children:"FieldValueFactor"})," using the ",(0,o.jsx)(n.code,{children:"fieldValueFactor"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val function: FieldValueFactor[Any] = fieldValueFactor(field = "field")\n'})}),"\n",(0,o.jsxs)(n.p,{children:["You can create a ",(0,o.jsx)(n.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"}),"\n",(0,o.jsx)(n.code,{children:"FieldValueFactor"})," using the ",(0,o.jsx)(n.code,{children:"fieldValueFactor"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"val function: FieldValueFactor[Document] = fieldValueFactor(field = Document.field)\n"})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["You can create ",(0,o.jsx)(n.code,{children:"RandomScoreFunction"})," using the ",(0,o.jsx)(n.code,{children:"randomScoreFunction"})," in three different ways depending on amount of parameters\nyou want to use:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val function: RandomScoreFunction[Any] = randomScoreFunction()\n\nval function: RandomScoreFunction[Any] = randomScoreFunction(seed = 10)\n\nval function: RandomScoreFunction[Any] = randomScoreFunction(seed = 10, field = "field")\n'})}),"\n",(0,o.jsxs)(n.p,{children:["You can create a ",(0,o.jsx)(n.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"}),"\n",(0,o.jsx)(n.code,{children:"RandomScoreFunction"})," using the ",(0,o.jsx)(n.code,{children:"randomScoreFunction"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"val function: RandomScoreFunction = randomScoreFunction(seed = 10, field = Document.field)\n"})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["You can create ",(0,o.jsx)(n.code,{children:"ScriptScoreFunction"})," using the ",(0,o.jsx)(n.code,{children:"scriptScoreFunction"})," method with script in following manner:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val function: ScriptScoreFunction[Any] = scriptScoreFunction(script = Script("params.agg1 > 10"))\nval function: ScriptScoreFunction[Any] = scriptScoreFunction(scriptSource = "params.agg1 > 10")\n'})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["You can create ",(0,o.jsx)(n.code,{children:"WeightFunction"})," using the ",(0,o.jsx)(n.code,{children:"weightFunction"})," method(you must provide ",(0,o.jsx)(n.code,{children:"Any"})," type parameter when using):"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"val function: WeightFunction[Any] = scriptScoreFunction(weight = 10)\n"})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["You can use these functions to create ",(0,o.jsx)(n.code,{children:"FunctionScore"})," query using the ",(0,o.jsx)(n.code,{children:"functionScore"})," method in the following manner:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"val randomScoreFunction: RandomScoreFunction[Any] = randomScoreFunction()\nval weightFunction: WeightFunction[Any] = scriptScoreFunction(weight = 10)\nval query: FunctionScoreQuery[Any] = functionScore(randomScoreFunction, weightFunction)\n"})}),"\n",(0,o.jsxs)(n.p,{children:["You can create a ",(0,o.jsx)(n.a,{href:"https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema",children:"type-safe"})," ",(0,o.jsx)(n.code,{children:"FunctionScore"})," query\nusing the ",(0,o.jsx)(n.code,{children:"functionScore"}),", if all functions are created type-safe, in the following manner:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'val decayFunction: DecayFunction[Document] = expDecayFunction(field = Document.field, origin = "11, 12", scale = "2km")\nval randomScoreFunction: RandomScoreFunction[Document] = randomScoreFunction(seed = 10, field = Document.field)\nval weightFunction: WeightFunction[Any] = scriptScoreFunction(weight = 10)\nval query: FunctionScoreQuery[Document] = functionScore(decayFunction, randomScoreFunction, weightFunction)\n'})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["If you want to change the ",(0,o.jsx)(n.code,{children:"boost"}),", you can use ",(0,o.jsx)(n.code,{children:"boost"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"import zio.elasticsearch.query.DistanceUnit\n\nval queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).boost(5.0)\n"})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["If you want to change the ",(0,o.jsx)(n.code,{children:"boostMode"}),", you can use ",(0,o.jsx)(n.code,{children:"boostMode"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"import zio.elasticsearch.query.DistanceUnit\n\nval queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).boostMode(FunctionScoreBoostMode.Max)\n"})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["If you want to change the ",(0,o.jsx)(n.code,{children:"maxBoost"}),", you can use ",(0,o.jsx)(n.code,{children:"maxBoost"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"import zio.elasticsearch.query.DistanceUnit\n\nval queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).maxBoost(5.0)\n"})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["If you want to change the ",(0,o.jsx)(n.code,{children:"minScore"}),", you can use ",(0,o.jsx)(n.code,{children:"minScore"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"import zio.elasticsearch.query.DistanceUnit\n\nval queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).minScore(5.0)\n"})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["If you want to change the ",(0,o.jsx)(n.code,{children:"query"}),", you can use ",(0,o.jsx)(n.code,{children:"query"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:'import zio.elasticsearch.query.DistanceUnit\n\nval queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).query(matches(Document.field, "value"))\n'})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["If you want to change the ",(0,o.jsx)(n.code,{children:"scoreMode"}),", you can use ",(0,o.jsx)(n.code,{children:"scoreMode"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"import zio.elasticsearch.query.DistanceUnit\n\nval queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).scoreMode(FunctionScoreScoreMode.Max)\n"})}),"\n",(0,o.jsx)("br",{}),"\n",(0,o.jsxs)(n.p,{children:["If you want to add a one or multiple new ",(0,o.jsx)(n.code,{children:"FunctionScoreFunction"})," you can use ",(0,o.jsx)(n.code,{children:"withFunctions"})," method:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-scala",children:"import zio.elasticsearch.query.DistanceUnit\n\nval queryWithDistance: FunctionScoreQuery[Document] = functionScore(randomScoreFunction(seed = 10, field = Document.field)).withFunctions(scriptScoreFunction(weight = 10))\n"})}),"\n",(0,o.jsxs)(n.p,{children:["You can find more information about ",(0,o.jsx)(n.code,{children:"FunctionScore"})," query ",(0,o.jsx)(n.a,{href:"https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-function-score-query.html",children:"here"}),"."]})]})}const d=function(e={}){const{wrapper:n}=Object.assign({},(0,i.a)(),e.components);return n?(0,o.jsx)(n,Object.assign({},e,{children:(0,o.jsx)(u,e)})):u(e)}},1151:(e,n,c)=>{c.d(n,{a:()=>t});var o=c(7294);const i={},r=o.createContext(i);function t(e){const n=o.useContext(r);return o.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}}}]);