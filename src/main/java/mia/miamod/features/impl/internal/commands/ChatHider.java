package mia.miamod.features.impl.internal.commands;

import kotlin.text.Regex;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ChatHider(Regex regex, Consumer<Matcher> callback) { }
