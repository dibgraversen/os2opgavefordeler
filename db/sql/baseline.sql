--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: distributionassignment; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE distributionassignment (
    id bigint NOT NULL,
    isactive boolean NOT NULL,
    validfrom date,
    validto date,
    employee_id bigint,
    org_id bigint
);


ALTER TABLE public.distributionassignment OWNER TO topicrouter;

--
-- Name: distributionrule; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE distributionrule (
    id bigint NOT NULL,
    assignedemp bigint NOT NULL,
    assignedorg_id bigint,
    kle_id bigint,
    municipality_id bigint NOT NULL,
    parent_id bigint,
    responsibleorg_id bigint
);


ALTER TABLE public.distributionrule OWNER TO topicrouter;

--
-- Name: employment; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE employment (
    id bigint NOT NULL,
    businesskey character varying(255),
    email character varying(255),
    esdhid character varying(255),
    esdhlabel character varying(255),
    initials character varying(255),
    isactive boolean NOT NULL,
    jobtitle character varying(255),
    name character varying(255),
    phone character varying(255),
    employedin_id bigint,
    municipality_id bigint
);


ALTER TABLE public.employment OWNER TO topicrouter;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: topicrouter
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO topicrouter;

--
-- Name: identityprovider; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE identityprovider (
    id integer NOT NULL,
    clientid character varying(255),
    clientsecret character varying(255),
    idpurl character varying(255),
    name character varying(255)
);


ALTER TABLE public.identityprovider OWNER TO topicrouter;

--
-- Name: kle; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE kle (
    id bigint NOT NULL,
    datecreated date NOT NULL,
    description text NOT NULL,
    number character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    parent_id bigint
);


ALTER TABLE public.kle OWNER TO topicrouter;

--
-- Name: municipality; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE municipality (
    id bigint NOT NULL,
    name character varying(255)
);


ALTER TABLE public.municipality OWNER TO topicrouter;

--
-- Name: orgunit; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE orgunit (
    id bigint NOT NULL,
    businesskey character varying(255) NOT NULL,
    email character varying(255),
    esdhid character varying(255),
    esdhlabel character varying(255),
    isactive boolean NOT NULL,
    name character varying(255),
    phone character varying(255),
    manager_id bigint,
    municipality_id bigint NOT NULL,
    parent_id bigint
);


ALTER TABLE public.orgunit OWNER TO topicrouter;

--
-- Name: role; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE role (
    id bigint NOT NULL,
    admin boolean NOT NULL,
    manager boolean NOT NULL,
    municipalityadmin boolean NOT NULL,
    name character varying(255),
    substitute boolean NOT NULL,
    employment_id bigint,
    owner_id bigint
);


ALTER TABLE public.role OWNER TO topicrouter;

--
-- Name: tr_user; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE tr_user (
    id bigint NOT NULL,
    email character varying(255),
    name character varying(255),
    municipality_id bigint,
    settings_id bigint
);


ALTER TABLE public.tr_user OWNER TO topicrouter;

--
-- Name: usersettings; Type: TABLE; Schema: public; Owner: topicrouter; Tablespace: 
--

CREATE TABLE usersettings (
    id bigint NOT NULL,
    scope character varying(255),
    showexpandedorg boolean NOT NULL,
    showresponsible boolean NOT NULL,
    userid bigint NOT NULL
);


ALTER TABLE public.usersettings OWNER TO topicrouter;

--
-- Name: distributionassignment_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY distributionassignment
    ADD CONSTRAINT distributionassignment_pkey PRIMARY KEY (id);


--
-- Name: distributionrule_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY distributionrule
    ADD CONSTRAINT distributionrule_pkey PRIMARY KEY (id);


--
-- Name: employment_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY employment
    ADD CONSTRAINT employment_pkey PRIMARY KEY (id);


--
-- Name: identityprovider_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY identityprovider
    ADD CONSTRAINT identityprovider_pkey PRIMARY KEY (id);


--
-- Name: kle_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY kle
    ADD CONSTRAINT kle_pkey PRIMARY KEY (id);


--
-- Name: municipality_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY municipality
    ADD CONSTRAINT municipality_pkey PRIMARY KEY (id);


--
-- Name: orgunit_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY orgunit
    ADD CONSTRAINT orgunit_pkey PRIMARY KEY (id);


--
-- Name: role_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: tr_user_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY tr_user
    ADD CONSTRAINT tr_user_pkey PRIMARY KEY (id);


--
-- Name: usersettings_pkey; Type: CONSTRAINT; Schema: public; Owner: topicrouter; Tablespace: 
--

ALTER TABLE ONLY usersettings
    ADD CONSTRAINT usersettings_pkey PRIMARY KEY (id);


--
-- Name: fk_1udl8myq0l93rqp02jjgc8i66; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY orgunit
    ADD CONSTRAINT fk_1udl8myq0l93rqp02jjgc8i66 FOREIGN KEY (municipality_id) REFERENCES municipality(id);


--
-- Name: fk_2mrgeooc0vhhq1shwotitt4t1; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY employment
    ADD CONSTRAINT fk_2mrgeooc0vhhq1shwotitt4t1 FOREIGN KEY (employedin_id) REFERENCES orgunit(id);


--
-- Name: fk_3krg0ud0xxvhpgymadwhpfse8; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY role
    ADD CONSTRAINT fk_3krg0ud0xxvhpgymadwhpfse8 FOREIGN KEY (employment_id) REFERENCES employment(id);


--
-- Name: fk_b1gjaqugmbgbostc4pb0tijhy; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY distributionrule
    ADD CONSTRAINT fk_b1gjaqugmbgbostc4pb0tijhy FOREIGN KEY (responsibleorg_id) REFERENCES orgunit(id);


--
-- Name: fk_bg6e0xy9ecxuhg65h4bb54le3; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY orgunit
    ADD CONSTRAINT fk_bg6e0xy9ecxuhg65h4bb54le3 FOREIGN KEY (parent_id) REFERENCES orgunit(id);


--
-- Name: fk_f4a3ynpwp7m5277n4lwusfsdq; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY distributionrule
    ADD CONSTRAINT fk_f4a3ynpwp7m5277n4lwusfsdq FOREIGN KEY (kle_id) REFERENCES kle(id);


--
-- Name: fk_g5itsnji56rki1tx7inf1qb8r; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY tr_user
    ADD CONSTRAINT fk_g5itsnji56rki1tx7inf1qb8r FOREIGN KEY (municipality_id) REFERENCES municipality(id);


--
-- Name: fk_gfren862ur5rbg4f603t0dc4b; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY kle
    ADD CONSTRAINT fk_gfren862ur5rbg4f603t0dc4b FOREIGN KEY (parent_id) REFERENCES kle(id);


--
-- Name: fk_jypbvhisio61ena2bddbyvs8x; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY tr_user
    ADD CONSTRAINT fk_jypbvhisio61ena2bddbyvs8x FOREIGN KEY (settings_id) REFERENCES usersettings(id);


--
-- Name: fk_lqcrfpiof758yjkt8malb6t5u; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY distributionassignment
    ADD CONSTRAINT fk_lqcrfpiof758yjkt8malb6t5u FOREIGN KEY (employee_id) REFERENCES employment(id);


--
-- Name: fk_m7a91vycaa7xbujw76jmnw57e; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY role
    ADD CONSTRAINT fk_m7a91vycaa7xbujw76jmnw57e FOREIGN KEY (owner_id) REFERENCES tr_user(id);


--
-- Name: fk_mfwyvw70sss0aba9x5nowvqnt; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY distributionrule
    ADD CONSTRAINT fk_mfwyvw70sss0aba9x5nowvqnt FOREIGN KEY (assignedorg_id) REFERENCES orgunit(id);


--
-- Name: fk_o24mtgfjesnched24h58yey5c; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY orgunit
    ADD CONSTRAINT fk_o24mtgfjesnched24h58yey5c FOREIGN KEY (manager_id) REFERENCES employment(id);


--
-- Name: fk_occgjv7gab6nn2129wn8iwl8a; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY employment
    ADD CONSTRAINT fk_occgjv7gab6nn2129wn8iwl8a FOREIGN KEY (municipality_id) REFERENCES municipality(id);


--
-- Name: fk_q5qo4cpfbg0tduymgawjn6dyg; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY distributionassignment
    ADD CONSTRAINT fk_q5qo4cpfbg0tduymgawjn6dyg FOREIGN KEY (org_id) REFERENCES orgunit(id);


--
-- Name: fk_r0wfdoyruvdsoh9gg8hlrfyps; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY distributionrule
    ADD CONSTRAINT fk_r0wfdoyruvdsoh9gg8hlrfyps FOREIGN KEY (parent_id) REFERENCES distributionrule(id);


--
-- Name: fk_tpy55dxiqr4f59h7icc9jso5s; Type: FK CONSTRAINT; Schema: public; Owner: topicrouter
--

ALTER TABLE ONLY distributionrule
    ADD CONSTRAINT fk_tpy55dxiqr4f59h7icc9jso5s FOREIGN KEY (municipality_id) REFERENCES municipality(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

