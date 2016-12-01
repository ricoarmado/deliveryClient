db.Factory.aggregate([
{$lookup:{from:"orders", localField:"_id", foreignField:"factory", as:"orders"}},
{$lookup:{from:"oilFactory", localField:"_id", foreignField:"factory", as:"oil"}},
{$match:{name:"׀מסֽופעü"}},
{$unwind: "$orders"},
{$unwind: "$oil"},
{$project: {"price":"$oil.price","Year":{ $substr:[ "$orders.date", 0, 4] }, "_id":0}},
{$group:{"_id":"$Year", "sum":{$sum: "$price"}}}
])