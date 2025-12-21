---@class File
local File = {}

---@return string
function File:getName() end

---@return Directory
function File:getParent() end

---@return boolean
function File:isFile() end

---@return integer[]
function File:getData() end

---@return string
function File:getDataAsString() end

return File
