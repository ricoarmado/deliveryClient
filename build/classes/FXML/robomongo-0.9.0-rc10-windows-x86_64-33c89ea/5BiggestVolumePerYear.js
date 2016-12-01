db.orders.aggregate([
{$lookup:{from:"Factory", localField:"factory", foreignField:"_id", as:"Factory"}},
{$unwind: "$Factory"},
{$project:{"factoryName":"$Factory.name","Year":{ $substr:[ "$date", 0, 4] }, "_id":0, "volume":1}},
{$match:{"Year": "2015"}},
{$sort:{"volume": -1}},
{$limit:5}
])