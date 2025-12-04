function execute(fileSystem, currentDir, arguments)
    for i, node in ipairs(currentDir:getNodes()) do
        print(node:getName());
    end
end

function description()
    return "Lists everything in a folder"
end