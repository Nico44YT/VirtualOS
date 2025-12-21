---@class Directory
local Directory = {}

---@return string
function Directory:getName() end

---@return Directory
function Directory:getParent() end

---@return boolean
function Directory:isDirectory() end

return Directory
