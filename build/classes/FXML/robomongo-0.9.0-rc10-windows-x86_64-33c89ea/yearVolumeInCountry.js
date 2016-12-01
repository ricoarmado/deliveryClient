db.Cartel.aggregate([
{$lookup:{from:"Factory", localField:"factoryID", foreignField:"_id", as:"Factory"}},
{$lookup:{from:"countries", localField:"countryID", foreignField:"_id", as:"Country"}},
{$unwind : "$Factory"},
{$unwind : "$Country"},
{$project:{"factory" : "$Factory.name", "country": "$Country.value", "factoryID":1, "_id":0}},
{$match:{country : "Россия"}},
{$lookup:{from:"oilFactory", localField:"factoryID", foreignField:"factory", as:"oil"}},
{$unwind: "$oil"},
{$project:{"volume": "$oil.volume", "factory":1, "country":1}},
{$group: {"_id": "$factory", "volume": {"$sum": "$volume"}}}
])