function execute(system, currentDir, arguments)
    for i, node in ipairs(currentDir:getNodes()) do
        print(node:getName() .. " | " .. system:getFormatedSize(node:getSize()));
    end
end

function description()
    return "Lists everything in a folder"
end