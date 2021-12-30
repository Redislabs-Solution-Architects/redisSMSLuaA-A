-- sets all fields for a hash from a dictionary 
local hmset = function (hkey, mkey, dict)
  if next(dict) == nil then return nil end
	local bulk = {}
	for k, v in pairs(dict) do
		table.insert(bulk, k)
		table.insert(bulk, v)
	end
        redis.call('SADD', mkey, hkey)
	return redis.call('HMSET', hkey, unpack(bulk))
end

return hmset(KEYS[1], KEYS[2],  cjson.decode(ARGV[1]))
