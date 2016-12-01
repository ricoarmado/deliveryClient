db.oilFactory.aggregate([
{$lookup:{from:"Factory", localField:"factory", foreignField:"_id", as:"factory"}},
{$lookup:{from:"oilTypes", localField:"oil", foreignField:"_id", as:"oil"}},
{$unwind: "$oil"},
{$unwind: "$factory"},
{$project: {"Oil": "$oil.id", "factory":"$factory.name", "volume": 1, "_id":0}},
{$sort: {"volume" : -1}},
{$limit : 10}
])