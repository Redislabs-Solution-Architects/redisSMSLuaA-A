-- sets all fields for a hash from a dictionary 
local hmset = function (key, dict)
  if next(dict) == nil then return nil end
	local bulk = {}
	for k, v in pairs(dict) do
		table.insert(bulk, k)
		table.insert(bulk, v)
	end
	return redis.call('HMSET', key, unpack(bulk))
end

return hmset(KEYS[1], cjson.decode(ARGV[1]))
