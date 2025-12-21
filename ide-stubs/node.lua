---@class Node
local Node = {}

---@return string
function Node:getName() end

---@return Directory
function Node:getParent() end

---@return boolean
function Node:isDirectory() end

---@return boolean
function Node:isFile() end

return Node
