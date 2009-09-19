#!/usr/bin/perl -w

use strict;
$/ = ";";

my %javasig = (
	"void" => "V",
	"glui32" => "J",
	"glsi32" => "I",
	"unsigned char" => "C",
	"winid_t" => "Lorg/andglk/Window;",
	"strid_t" => "Lorg/andglk/Stream;",
	"frefid_t" => "Lorg/andglk/FileRef;"
);

my %javatypes = (
	"void" => "Void",
	"glui32" => "Long",
	"glsi32" => "Int",
	"unsigned char" => "Char",
	"winid_t" => "Object",
	"strid_t" => "Object",
	"frefid_t" => "Object"
);

sub t2sig($)
{
	my $arg = shift;
	$javasig{$arg} || "[FIXME: $arg]";
}

sub t2java($)
{
	my $arg = shift;
	$javatypes{$arg} || "/*[FIXME: $arg]*/";
}


while (<>) {
	if ($_ =~ /extern ((.*?) ([a-z0-9_A-Z]+)\((.*?)\));/s) {
		my ($decl, $rettype, $name, $argsstring) = ($1, $2, $3, $4);
		
		my @args = split(/\s*,\s*/s, $argsstring);
		@args = () if ($#args == 0 && $args[0] eq "void");
		my (@argtypes, @argnames);
		map { /^(.*?)\s*([a-zA-Z0-9_]+$)/; push @argtypes, $1; push @argnames, $2; } @args;
		
		my $signature = "(".join("", map { t2sig($_) } @argtypes).")";
		$signature .= t2sig($rettype);
		my $call = "";
		$call = "return " unless $rettype eq "void";
		$call .= "(*env)->Call".t2java($rettype)."Method(env, _this, mid".(@argnames && (", ".join(", ", @argnames)) || "").");\n";
		
		print << "EEE"
$decl
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "$name", "$signature");

	$call
}

EEE
	}
}
