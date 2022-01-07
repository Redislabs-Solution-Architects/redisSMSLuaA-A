-- sets all fields for a hash from a dictionary 
local hmset = function (hkey, mkey, skey, dict, appName, messageId)
  if next(dict) == nil then return nil end
	local bulk = {}
	for k, v in pairs(dict) do
		table.insert(bulk, k)
		table.insert(bulk, v)
	end
    redis.call('SADD', mkey, hkey)
    redis.log(redis.LOG_WARNING,"in hmset.lua")
    redis.log(redis.LOG_WARNING,unpack(bulk))
    redis.log(redis.LOG_WARNING,messageId)
    redis.call('XACK', skey, appName, messageId )
	return redis.call('HMSET', hkey, unpack(bulk))
end

return hmset(KEYS[1], KEYS[2], KEYS[3], cjson.decode(ARGV[1]), ARGV[2], ARGV[3])
