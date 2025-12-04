function execute(fileSystem, currentDir, arguments)
    for i, command in ipairs(fileSystem:getCommands()) do
        print(command:getName() .. " | " .. command:getDescription())
    end
end

function description()
    return "Explains Commands"
end