package zio

package object elasticsearch {

  val putBody: String =
    """
      |{
      |   "category":[
      |      "Men's Accessories",
      |      "Men's Shoes"
      |   ],
      |   "currency":"RSD",
      |   "customer_first_name":"Abd",
      |   "customer_full_name":"Abd Bryan",
      |   "customer_gender":"MALE",
      |   "customer_id":52,
      |   "customer_last_name":"Bryan",
      |   "customer_phone":"",
      |   "day_of_week":"Thursday",
      |   "day_of_week_i":3,
      |   "email":"abd@bryan-family.zzz",
      |   "manufacturer":[
      |      "Elitelligence",
      |      "Low Tide Media"
      |   ],
      |   "order_date":"2022-11-24T10:06:14+00:00",
      |   "order_id":569905,
      |   "products":[
      |      {
      |         "base_price":10.99,
      |         "discount_percentage":0,
      |         "quantity":1,
      |         "manufacturer":"Elitelligence",
      |         "tax_amount":0,
      |         "product_id":11149,
      |         "category":"Men's Accessories",
      |         "sku":"ZO0599605996",
      |         "taxless_price":10.99,
      |         "unit_discount_amount":0,
      |         "min_price":5.06,
      |         "_id":"sold_product_569905_11149",
      |         "discount_amount":0,
      |         "created_on":"2016-12-15T10:06:14+00:00",
      |         "product_name":"Sunglasses - black",
      |         "price":10.99,
      |         "taxful_price":10.99,
      |         "base_unit_price":10.99
      |      },
      |      {
      |         "base_price":64.99,
      |         "discount_percentage":0,
      |         "quantity":1,
      |         "manufacturer":"Low Tide Media",
      |         "tax_amount":0,
      |         "product_id":18515,
      |         "category":"Men's Shoes",
      |         "sku":"ZO0403804038",
      |         "taxless_price":64.99,
      |         "unit_discount_amount":0,
      |         "min_price":31.85,
      |         "_id":"sold_product_569905_18515",
      |         "discount_amount":0,
      |         "created_on":"2016-12-15T10:06:14+00:00",
      |         "product_name":"Lace-up boots - tan",
      |         "price":64.99,
      |         "taxful_price":64.99,
      |         "base_unit_price":64.99
      |      }
      |   ],
      |   "sku":[
      |      "ZO0599605996",
      |      "ZO0403804038"
      |   ],
      |   "taxful_total_price":75.98,
      |   "taxless_total_price":75.98,
      |   "total_quantity":2,
      |   "total_unique_products":2,
      |   "type":"order",
      |   "user":"abd",
      |   "geoip":{
      |      "country_iso_code":"EG",
      |      "location":{
      |         "lon":31.3,
      |         "lat":30.1
      |      },
      |      "region_name":"Cairo Governorate",
      |      "continent_name":"Africa",
      |      "city_name":"Cairo"
      |   },
      |   "event":{
      |      "dataset":"sample_ecommerce"
      |   }
      |}
      |""".stripMargin

}
